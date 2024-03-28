package uk.co.nstauthority.fieldconsents.application.submission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationServiceTest.USER;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.UPDATE_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.getConsentDetailsTaskListItemsWithLabel;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.getConsentDetailsTaskListSection;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListLabel;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionServiceTest {

  @Mock
  private ApplicationTaskListService applicationTaskListService;

  @Mock
  private ApplicationRepository applicationRepository;

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private AceFlagService aceFlagService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationSubmissionEmailService applicationSubmissionEmailService;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private Clock clock;


  private ApplicationSubmissionService applicationSubmissionService;

  private ApplicationVersion applicationVersion;

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
        applicationSubmissionEmailService,
        applicationWorkAreaPriorityService);
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    taskListSections = new ArrayList<>();
  }

  @Test
  void isSubmittable_allSectionsCompleted() {
    when(applicationTaskListService.getAllSections(applicationVersion)).thenReturn(taskListSections);

    var taskListItems = getConsentDetailsTaskListItemsWithLabel(
        applicationVersion.getApplication().getId(), TaskListLabel.COMPLETED);

    taskListSections.add(getConsentDetailsTaskListSection(taskListItems));

    assertThat(applicationSubmissionService.isSubmittable(applicationVersion)).isTrue();
  }

  @Test
  void isSubmittable_withSectionsNotCompleted() {
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
  void submitApplication_statusInProgressOrAwaitingPaymentAndApplicationIsNonAceAndHasNullNumber(
      ApplicationVersionStatus applicationVersionStatus
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationVersion.setStatus(applicationVersionStatus);

    var application = applicationVersion.getApplication();

    application.setApplicationNo(null);

    when(applicationService.getNextApplicationNumber()).thenReturn(2);

    applicationSubmissionService.submitApplication(applicationVersion, USER);

    ArgumentCaptor<Application> applicationArgumentCaptor = ArgumentCaptor.forClass(Application.class);
    verify(applicationRepository).save(applicationArgumentCaptor.capture());

    var actualApplication = applicationArgumentCaptor.getValue();

    assertThat(actualApplication.getVariationNo()).isZero();
    assertThat(actualApplication.getApplicationNo()).isEqualTo(2);

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, REGULATOR);
    verify(aceFlagService)
        .autoSetAceFlag(applicationVersion);
    verify(applicationSubmissionEmailService).sendNonAceApplicationSubmissionEmail(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = { "IN_PROGRESS", "AWAITING_PAYMENT" })
  void submitApplication_statusInProgressOrAwaitingPaymentAndApplicationIsNonAceAndHasNonNullNumber(
      ApplicationVersionStatus applicationVersionStatus
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationVersion.setStatus(applicationVersionStatus);

    var application = applicationVersion.getApplication();

    application.setApplicationNo(7);

    applicationSubmissionService.submitApplication(applicationVersion, USER);

    ArgumentCaptor<Application> applicationArgumentCaptor = ArgumentCaptor.forClass(Application.class);
    verify(applicationRepository).save(applicationArgumentCaptor.capture());

    var actualApplication = applicationArgumentCaptor.getValue();

    assertThat(actualApplication.getVariationNo()).isZero();
    assertThat(actualApplication.getApplicationNo()).isEqualTo(7);

    verify(applicationRepository, never()).findLatestNonMigratedApplicationNumber();
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, REGULATOR);
    verify(aceFlagService)
        .autoSetAceFlag(applicationVersion);
    verify(applicationSubmissionEmailService).sendNonAceApplicationSubmissionEmail(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = { "IN_PROGRESS", "AWAITING_PAYMENT" })
  void submitApplication_statusInProgressOrAwaitingPaymentAndApplicationIsAce(
      ApplicationVersionStatus applicationVersionStatus
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setStatus(applicationVersionStatus);

    applicationSubmissionService.submitApplication(applicationVersion, USER);

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, REGULATOR);
    verify(aceFlagService)
        .autoSetAceFlag(applicationVersion);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationVersionStatus.class, names = { "IN_PROGRESS", "AWAITING_PAYMENT" })
  void submitApplication_sendNonAceApplicationSubmissionEmail_thenApplicationIsStillSubmitted(
      ApplicationVersionStatus applicationVersionStatus
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setStatus(applicationVersionStatus);

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
    var draftApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 2, 2);

    applicationSubmissionService.submitApplicationUpdate(draftApplicationVersion, USER);

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(draftApplicationVersion, USER, UPDATE_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(draftApplicationVersion, USER, UPDATE_SUBMITTED, REGULATOR);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(draftApplicationVersion, USER, UPDATE_SUBMITTED, REGULATOR_TECHNICAL_REVIEWER);
  }

  @Test
  void submitApplicationVersion() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationSubmissionService.submitApplicationVersion(applicationVersion, USER);

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);
    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    var actualApplicationVersion = applicationVersionArgumentCaptor.getValue();

    assertThat(actualApplicationVersion.getId()).isEqualTo(applicationVersion.getId());
    assertThat(actualApplicationVersion.getVersion()).isEqualTo(applicationVersion.getVersion());
    assertThat(actualApplicationVersion.getCreatedByWuaId()).isEqualTo(applicationVersion.getCreatedByWuaId());
    assertThat(actualApplicationVersion.getCreatedDateTime()).isEqualTo(applicationVersion.getCreatedDateTime());
    assertThat(actualApplicationVersion.getPrimaryOperatorOuId()).isEqualTo(applicationVersion.getPrimaryOperatorOuId());
    assertThat(actualApplicationVersion.getCachedPrimaryOperatorName()).isEqualTo(applicationVersion.getCachedPrimaryOperatorName());
    assertThat(actualApplicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.SUBMITTED);
    assertThat(actualApplicationVersion.getSubmittedByWuaId()).isEqualTo(USER_WUA_ID);
  }
}
