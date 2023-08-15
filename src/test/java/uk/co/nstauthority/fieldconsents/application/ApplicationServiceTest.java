package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationService.NOT_LATEST_APPLICATION_VERSION_ERROR_MESSAGE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationService.START_APPLICATION_UPDATE_ERROR_MESSAGE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_CREATED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.UPDATE_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

  private static final Instant CURRENT_INSTANT = Instant.now();

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String APPLICATION_NUMBER_START_VALUE = "50";

  private Application newApplication;

  @Mock
  private ApplicationRepository applicationRepository;

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationAssetLicenceService applicationAssetLicenceService;

  @Mock
  private AceFlagService aceFlagService;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private Clock clock;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private TechnicalReviewService technicalReviewService;

  private ApplicationService applicationService;

  @BeforeEach
  void setup() {
    ApplicationConfigurationProperties applicationConfigurationProperties = new ApplicationConfigurationProperties(
        APPLICATION_NUMBER_START_VALUE);

    applicationService = new ApplicationService(
        applicationRepository,
        applicationVersionRepository,
        applicationAssetService,
        applicationAssetLicenceService,
        applicationConfigurationProperties,
        aceFlagService,
        applicationWorkAreaPriorityService,
        clock,
        applicationVersionService,
        technicalReviewService
    );

    newApplication = new Application(1, ApplicationType.PRODUCTION, Instant.now(), USER_WUA_ID, 0, null);
  }

  @Test
  void createNewApplicationForField() {
    OrganisationUnitJson organisationUnitJson = OrganisationUnitTestUtil.orgUnit1Json;
    when(applicationRepository.save(any(Application.class))).thenReturn(newApplication);

    ApplicationVersion newApplicationVersion = new ApplicationVersion(1, newApplication, 1, organisationUnitJson.organisationUnitId(),
        organisationUnitJson.name(), Instant.now(), USER_WUA_ID, null, null, ApplicationVersionStatus.IN_PROGRESS, null);
    when(applicationVersionRepository.save(any(ApplicationVersion.class))).thenReturn(newApplicationVersion);

    ApplicationAsset applicationAsset = ApplicationAssetTestUtil.fieldAsset1;
    when(applicationAssetService.createPrimaryAsset(newApplicationVersion, field1JsonWithOperatorAndLicences))
        .thenReturn(applicationAsset);

    ApplicationVersion expectedApplicationVersion = applicationService.createNewApplicationForField(
        ApplicationType.PRODUCTION,
        field1JsonWithOperatorAndLicences,
        organisationUnitJson,
        USER
    );

    assertApplicationVersion(newApplicationVersion, expectedApplicationVersion);

    verify(applicationAssetService).createPrimaryAsset(
        newApplicationVersion,
        field1JsonWithOperatorAndLicences
    );

    verify(applicationAssetLicenceService)
        .createAssetLicences(applicationAsset, field1JsonWithOperatorAndLicences);

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(expectedApplicationVersion, USER, APPLICATION_CREATED, INDUSTRY);
  }

  @Test
  void createNewApplicationForTerminal() {
    OrganisationUnitJson organisationUnitJson = OrganisationUnitTestUtil.orgUnit1Json;
    when(applicationRepository.save(any(Application.class))).thenReturn(newApplication);

    ApplicationVersion newApplicationVersion = new ApplicationVersion(1, newApplication, 1, organisationUnitJson.organisationUnitId(),
        organisationUnitJson.name(), Instant.now(), USER_WUA_ID, null, null, ApplicationVersionStatus.IN_PROGRESS, null);
    when(applicationVersionRepository.save(any(ApplicationVersion.class))).thenReturn(newApplicationVersion);

    ApplicationVersion expectedApplicationVersion = applicationService.createNewApplicationForTerminal(
        ApplicationType.PRODUCTION,
        terminal1JsonWithOperator,
        organisationUnitJson,
        USER
    );

    assertApplicationVersion(newApplicationVersion, expectedApplicationVersion);

    verify(applicationAssetService).createPrimaryAsset(
        newApplicationVersion,
        terminal1JsonWithOperator
    );

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(expectedApplicationVersion, USER, APPLICATION_CREATED, INDUSTRY);
  }

  private void assertApplicationVersion(ApplicationVersion newApplicationVersion, ApplicationVersion expectedApplicationVersion) {
    assertThat(expectedApplicationVersion.getId()).isEqualTo(newApplicationVersion.getId());
    assertThat(expectedApplicationVersion.getVersion()).isEqualTo(newApplicationVersion.getVersion());
    assertThat(expectedApplicationVersion.getPrimaryOperatorOuId())
        .isEqualTo(newApplicationVersion.getPrimaryOperatorOuId());
    assertThat(expectedApplicationVersion.getCachedPrimaryOperatorName())
        .isEqualTo(newApplicationVersion.getCachedPrimaryOperatorName());
    assertThat(expectedApplicationVersion.getCreatedByWuaId()).isEqualTo(newApplicationVersion.getCreatedByWuaId());
    assertThat(expectedApplicationVersion.getCreatedDateTime()).isAfterOrEqualTo(newApplicationVersion.getCreatedDateTime());
    assertThat(expectedApplicationVersion.getStatus()).isEqualTo(newApplicationVersion.getStatus());

    if (ApplicationVersionStatus.SUBMITTED.equals(expectedApplicationVersion.getStatus())) {
      assertThat(expectedApplicationVersion.getSubmittedByWuaId()).isEqualTo(newApplicationVersion.getSubmittedByWuaId());
      assertThat(expectedApplicationVersion.getSubmittedDateTime()).isAfterOrEqualTo(newApplicationVersion.getSubmittedDateTime());
    } else {
      assertThat(expectedApplicationVersion.getSubmittedByWuaId()).isNull();
      assertThat(expectedApplicationVersion.getSubmittedDateTime()).isNull();
    }

    if (Objects.isNull((newApplicationVersion.getCaseOfficerWuaId()))) {
      assertThat(expectedApplicationVersion.getCaseOfficerWuaId()).isNull();
    } else {
      assertThat(expectedApplicationVersion.getCaseOfficerWuaId()).isEqualTo(newApplicationVersion.getCaseOfficerWuaId());
    }

    var expectedApplication = expectedApplicationVersion.getApplication();
    var newApplication = newApplicationVersion.getApplication();
    assertThat(expectedApplication.getType()).isEqualTo(newApplication.getType());
    assertThat(expectedApplication.getCreatedByWuaId()).isEqualTo(newApplication.getCreatedByWuaId());
    assertThat(expectedApplication.getCreatedDate()).isEqualTo(newApplication.getCreatedDate());
  }

  @Test
  void getApplicationById_whenApplicationExists() {
    when(applicationRepository.findById(anyInt())).thenReturn(Optional.of(newApplication));
    Application expectedApplication = applicationService.getApplicationById(1);

    assertThat(expectedApplication.getId()).isEqualTo(newApplication.getId());
    assertThat(expectedApplication.getType()).isEqualTo(newApplication.getType());
    assertThat(expectedApplication.getCreatedDate()).isEqualTo(newApplication.getCreatedDate());
    assertThat(expectedApplication.getCreatedByWuaId()).isEqualTo(newApplication.getCreatedByWuaId());
  }

  @Test
  void getApplicationById_whenApplicationIsNotFound() {
    when(applicationRepository.findById(anyInt())).thenReturn(Optional.empty());

    var exception = Assertions.assertThrows(
        EntityNotFoundException.class,
        () -> applicationService.getApplicationById(1)
    );

    Assertions.assertEquals("Application with id 1 not found", exception.getMessage());
  }

  @Test
  void submitApplication() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationRepository.findLatestApplicationNumber()).thenReturn(Optional.of(1));

    applicationService.submitApplication(applicationVersion, USER);

    ArgumentCaptor<Application> applicationArgumentCaptor = ArgumentCaptor.forClass(Application.class);
    verify(applicationRepository).save(applicationArgumentCaptor.capture());

    var actualApplication = applicationArgumentCaptor.getValue();

    assertThat(actualApplication.getApplicationNo()).isEqualTo(2);
    assertThat(actualApplication.getVariationNo()).isEqualTo(0);

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(applicationVersion, USER, APPLICATION_SUBMITTED, REGULATOR);
    verify(aceFlagService)
        .autoSetAceFlag(applicationVersion);
  }

  @Test
  void submitApplicationUpdate_withNoOpenTechnicalReview() {
    var draftApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 2, 2);
    when(technicalReviewService.openTechnicalReviewExists(draftApplicationVersion))
        .thenReturn(false);

    applicationService.submitApplicationUpdate(draftApplicationVersion, USER);

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(draftApplicationVersion, USER, UPDATE_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(draftApplicationVersion, USER, UPDATE_SUBMITTED, REGULATOR);
  }

  @Test
  void submitApplicationUpdate_withTechnicalReviewOpen() {
    var draftApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 2, 2);
    when(technicalReviewService.openTechnicalReviewExists(draftApplicationVersion))
        .thenReturn(true);

    applicationService.submitApplicationUpdate(draftApplicationVersion, USER);

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(draftApplicationVersion, USER, UPDATE_SUBMITTED, INDUSTRY);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(draftApplicationVersion, USER, UPDATE_SUBMITTED, REGULATOR);
    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(draftApplicationVersion, USER, UPDATE_SUBMITTED, REGULATOR_TECHNICAL_REVIEWER);
  }

  @Test
  void startApplicationUpdate_whenNotLatestAppVersionSupplied_thenError() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var latestApplicationVersion =
        ApplicationTestUtil.getSubmittedApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.PRODUCTION, 2, 2);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationVersion.getId()))
        .thenReturn(latestApplicationVersion);

    assertThatThrownBy(() -> applicationService.startApplicationUpdate(applicationVersion, USER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(NOT_LATEST_APPLICATION_VERSION_ERROR_MESSAGE.formatted(
            applicationVersion.getId(),
            latestApplicationVersion.getId()));
  }

  @Test
  void startApplicationUpdate_whenUnexpectedStatus_thenError() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationVersion.getId()))
        .thenReturn(applicationVersion);

    assertThatThrownBy(() -> applicationService.startApplicationUpdate(applicationVersion, USER))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(START_APPLICATION_UPDATE_ERROR_MESSAGE.formatted(
            applicationVersion.getId(),
            applicationVersion.getStatus().getDisplayName(),
            ApplicationVersionStatus.SUBMITTED.name()));
  }

  @Test
  void startApplicationUpdate() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(applicationVersion.getId()))
        .thenReturn(applicationVersion);
    var expectedApplicationVersion = new ApplicationVersion();
    expectedApplicationVersion.setApplication(applicationVersion.getApplication());
    expectedApplicationVersion.setVersion(applicationVersion.getVersion() + 1);
    expectedApplicationVersion.setCreatedDateTime(clock.instant());
    expectedApplicationVersion.setCreatedByWuaId(USER.wuaId());
    expectedApplicationVersion.setStatus(ApplicationVersionStatus.IN_PROGRESS);
    expectedApplicationVersion.setPrimaryOperatorOuId(applicationVersion.getPrimaryOperatorOuId());
    expectedApplicationVersion.setCachedPrimaryOperatorName(applicationVersion.getCachedPrimaryOperatorName());
    expectedApplicationVersion.setCaseOfficerWuaId(applicationVersion.getCaseOfficerWuaId());
    when(applicationVersionRepository.save(any())).thenReturn(expectedApplicationVersion);

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationVersion.class);

    var newApplicationVersion = applicationService.startApplicationUpdate(applicationVersion, USER);
    verify(applicationVersionRepository).save(applicationVersionArgumentCaptor.capture());

    assertApplicationVersion(newApplicationVersion, expectedApplicationVersion);
  }

  @Test
  void submitApplicationVersion_whenAlreadySubmitted() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    var exception = Assertions.assertThrows(
        IllegalStateException.class,
        () -> applicationService.submitApplicationVersion(applicationVersion, USER)
    );

    Assertions.assertEquals("Application with id 1 cannot be submitted", exception.getMessage());
  }
  
  @Test
  void submitApplicationVersion() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationService.submitApplicationVersion(applicationVersion, USER);

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

  @Test
  void generateApplicationReference_forProductionApplication() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    assertThat(applicationService.generateApplicationReference(applicationVersion)).isEqualTo("PCON/500/0 (Version 1)");
  }

  @Test
  void generateApplicationReference_forVentApplication() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);

    assertThat(applicationService.generateApplicationReference(applicationVersion)).isEqualTo("VCON/500/0 (Version 1)");
  }

  @Test
  void generateApplicationReference_forFlareApplication() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);

    assertThat(applicationService.generateApplicationReference(applicationVersion)).isEqualTo("FCON/500/0 (Version 1)");
  }

  @Test
  void getApplicationReference_withInProgressApplication() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    assertThat(applicationService.getApplicationReference(applicationVersion)).isEqualTo("");
  }

  @Test
  void getApplicationReference_withSubmittedApplication() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);

    assertThat(applicationService.getApplicationReference(applicationVersion)).isEqualTo("FCON/500/0 (Version 1)");
  }

  @Test
  void getApplicationNumber_whenOneApplicationExists() {
    when(applicationRepository.findLatestApplicationNumber()).thenReturn(Optional.of(1));

    assertThat(applicationService.getApplicationNumber()).isEqualTo(2);
  }

  @Test
  void getApplicationNumber_whenNoApplicationExists() {
    when(applicationRepository.findLatestApplicationNumber()).thenReturn(Optional.empty());

    assertThat(applicationService.getApplicationNumber()).isEqualTo(Integer.parseInt(APPLICATION_NUMBER_START_VALUE));
  }
}
