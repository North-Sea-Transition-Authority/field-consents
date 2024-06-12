package uk.co.nstauthority.fieldconsents.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationServiceTest.USER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.UPDATE_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.getConsentDetailsTaskListItemsWithLabel;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.getConsentDetailsTaskListSection;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationSnsService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionServiceTest {

  @Mock
  private AceFlagService aceFlagService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationRepository applicationRepository;

  @Mock
  private ApplicationTaskListService applicationTaskListService;

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private ApplicationSnsService applicationSnsService;

  @Mock
  private ApplicationSubmissionEmailService applicationSubmissionEmailService;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationSubmissionService applicationSubmissionService;

  private List<TaskListSection> taskListSections;

  @BeforeEach
  void setUp() {
    applicationSubmissionService = new ApplicationSubmissionService(
        clock,
        aceFlagService,
        applicationService,
        applicationRepository,
        applicationTaskListService,
        applicationVersionRepository,
        applicationSnsService,
        applicationSubmissionEmailService,
        applicationWorkAreaPriorityService,
        energyPortalUserService
    );

    taskListSections = new ArrayList<>();
  }

  @Test
  void isSubmittable_allSectionsCompleted() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(taskListSections);

    var taskListItems = getConsentDetailsTaskListItemsWithLabel(
        applicationVersion.getApplication().getId(), TaskListLabel.COMPLETED);

    taskListSections.add(getConsentDetailsTaskListSection(taskListItems));

    assertThat(applicationSubmissionService.isSubmittable(applicationVersion)).isTrue();
  }

  @Test
  void isSubmittable_withSectionsNotCompleted() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(taskListSections);

    var taskListItems = getConsentDetailsTaskListItemsWithLabel(
        applicationVersion.getApplication().getId(), TaskListLabel.NOT_STARTED);

    taskListSections.add(getConsentDetailsTaskListSection(taskListItems));

    assertThat(applicationSubmissionService.isSubmittable(applicationVersion)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(
      value = ApplicationVersionStatus.class,
      names = { "IN_PROGRESS", "AWAITING_PAYMENT" },
      mode = EnumSource.Mode.EXCLUDE
  )
  void submitApplication_statusNotInProgressOrAwaitingPayment(ApplicationVersionStatus applicationVersionStatus) {
    var applicationVersion
        = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 1, 1);
    applicationVersion.setStatus(applicationVersionStatus);

    assertThatThrownBy(() -> applicationSubmissionService.submitApplication(applicationVersion, USER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            String.format(
                "Application %d cannot be submitted as application version has status %s",
                applicationVersion.getApplication().getId(),
                applicationVersionStatus
            )
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = { "IN_PROGRESS", "AWAITING_PAYMENT" })
  void submitApplication_statusInProgressOrAwaitingPaymentAndApplicationHasNullNumberAndIsNonAce(
      ApplicationVersionStatus applicationVersionStatus
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationVersion.setStatus(applicationVersionStatus);

    var application = applicationVersion.getApplication();

    application.setApplicationNo(null);

    when(applicationService.getNextApplicationNumber()).thenReturn(2);
    when(aceFlagService.isAceApplication(applicationVersion)).thenReturn(false);

    applicationSubmissionService.submitApplication(applicationVersion, USER);

    assertThat(application.getApplicationNo()).isEqualTo(2);
    verify(applicationRepository).save(application);

    assertThat(applicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.SUBMITTED);
    assertThat(applicationVersion.getSubmittedDateTime()).isEqualTo(clock.instant());
    assertThat(applicationVersion.getSubmittedByWuaId()).isEqualTo(USER.wuaId());
    verify(applicationVersionRepository).save(applicationVersion);

    verify(applicationSnsService).publishApplicationSubmittedSnsMessage(applicationVersion);
    verify(aceFlagService).autoSetAceFlag(applicationVersion);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, REGULATOR);
    verify(applicationSubmissionEmailService).sendNonAceApplicationSubmissionEmail(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = { "IN_PROGRESS", "AWAITING_PAYMENT" })
  void submitApplication_statusInProgressOrAwaitingPaymentAndApplicationHasNonNullNumberAndIsNonAce(
      ApplicationVersionStatus applicationVersionStatus
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationVersion.setStatus(applicationVersionStatus);

    var application = applicationVersion.getApplication();

    application.setApplicationNo(7);

    when(aceFlagService.isAceApplication(applicationVersion)).thenReturn(false);

    applicationSubmissionService.submitApplication(applicationVersion, USER);

    assertThat(application.getApplicationNo()).isEqualTo(7);
    verify(applicationService, never()).getNextApplicationNumber();
    verify(applicationRepository, never()).save(any());

    assertThat(applicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.SUBMITTED);
    assertThat(applicationVersion.getSubmittedDateTime()).isEqualTo(clock.instant());
    assertThat(applicationVersion.getSubmittedByWuaId()).isEqualTo(USER.wuaId());
    verify(applicationVersionRepository).save(applicationVersion);

    verify(applicationSnsService).publishApplicationSubmittedSnsMessage(applicationVersion);
    verify(aceFlagService).autoSetAceFlag(applicationVersion);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, REGULATOR);
    verify(applicationSubmissionEmailService).sendNonAceApplicationSubmissionEmail(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = { "IN_PROGRESS", "AWAITING_PAYMENT" })
  void submitApplication_statusInProgressOrAwaitingPaymentAndApplicationIsNonAce_sendNonAceApplicationSubmissionEmailThrowsError_thenApplicationIsStillSubmitted(
      ApplicationVersionStatus applicationVersionStatus
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setStatus(applicationVersionStatus);

    when(aceFlagService.isAceApplication(applicationVersion)).thenReturn(false);

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(applicationSubmissionEmailService)
        .sendNonAceApplicationSubmissionEmail(applicationVersion);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () ->  applicationSubmissionService.submitApplication(applicationVersion, USER));

    verify(applicationSubmissionEmailService).sendNonAceApplicationSubmissionEmail(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = { "IN_PROGRESS", "AWAITING_PAYMENT" })
  void submitApplication_statusInProgressOrAwaitingPaymentAndApplicationIsAce(
      ApplicationVersionStatus applicationVersionStatus
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setStatus(applicationVersionStatus);

    when(aceFlagService.isAceApplication(applicationVersion)).thenReturn(true);

    applicationSubmissionService.submitApplication(applicationVersion, USER);

    verify(applicationSubmissionEmailService, never()).sendNonAceApplicationSubmissionEmail(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(
      value = ApplicationVersionStatus.class,
      names = { "IN_PROGRESS" },
      mode = EnumSource.Mode.EXCLUDE
  )
  void regulatorAutoSubmitApplication_statusNotInProgress(ApplicationVersionStatus applicationVersionStatus) {
    var applicationVersion
        = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 1, 2);
    applicationVersion.setStatus(applicationVersionStatus);

    var previousApplicationVersion =
        ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 2, 1);

    assertThatThrownBy(() -> applicationSubmissionService.regulatorAutoSubmitApplication(applicationVersion, previousApplicationVersion, USER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            String.format(
                "Application %d cannot be submitted as application version has status %s",
                applicationVersion.getApplication().getId(),
                applicationVersionStatus
            )
        );
  }

  @Test
  void regulatorAutoSubmitApplication_statusInProgress() {
    var applicationVersion
        = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 1, 2);

    var previousApplicationVersion =
        ApplicationTestUtil.getSubmittedApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.FLARE, 2, 1);

    var previousSubmittedByUserEnergyPortalUserDto = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(10L)
        .build();
    var previousSubmittedByUser = ServiceUserDetail.from(previousSubmittedByUserEnergyPortalUserDto);

    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(previousApplicationVersion.getSubmittedByWuaId())))
        .thenReturn(previousSubmittedByUserEnergyPortalUserDto);

    applicationSubmissionService.regulatorAutoSubmitApplication(applicationVersion, previousApplicationVersion, USER);

    assertThat(applicationVersion.getAutoSubmittedByWuaId()).isEqualTo(USER.wuaId());
    assertThat(applicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.SUBMITTED);
    assertThat(applicationVersion.getSubmittedDateTime()).isEqualTo(clock.instant());
    assertThat(applicationVersion.getSubmittedByWuaId()).isEqualTo(previousSubmittedByUser.wuaId());
    verify(applicationVersionRepository).save(applicationVersion);

    verify(applicationSnsService).publishApplicationSubmittedSnsMessage(applicationVersion);
    verify(aceFlagService).autoSetAceFlag(applicationVersion);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, previousSubmittedByUser, APPLICATION_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, previousSubmittedByUser, APPLICATION_SUBMITTED, REGULATOR);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = "IN_PROGRESS", mode = EnumSource.Mode.EXCLUDE)
  void submitApplicationUpdate_statusNotInProgress(ApplicationVersionStatus applicationVersionStatus) {
    var applicationVersion
        = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 1, 1);
    applicationVersion.setStatus(applicationVersionStatus);

    assertThatThrownBy(() -> applicationSubmissionService.submitApplicationUpdate(applicationVersion, USER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            String.format(
                "Application update cannot be submitted for application %d as application version has status %s",
                applicationVersion.getApplication().getId(),
                applicationVersionStatus
            )
        );
  }

  @Test
  void submitApplicationUpdate() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 2, 2);

    applicationSubmissionService.submitApplicationUpdate(applicationVersion, USER);

    assertThat(applicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.SUBMITTED);
    assertThat(applicationVersion.getSubmittedDateTime()).isEqualTo(clock.instant());
    assertThat(applicationVersion.getSubmittedByWuaId()).isEqualTo(USER.wuaId());
    verify(applicationVersionRepository).save(applicationVersion);

    verify(applicationSnsService).publishApplicationSubmittedSnsMessage(applicationVersion);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, UPDATE_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, UPDATE_SUBMITTED, REGULATOR);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, UPDATE_SUBMITTED, REGULATOR_TECHNICAL_REVIEWER);
  }
}
