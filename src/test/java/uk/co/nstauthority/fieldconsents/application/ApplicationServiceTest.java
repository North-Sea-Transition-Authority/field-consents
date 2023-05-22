package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

  private static final String APPLICATION_NUMBER_START_VALUE = "50";

  private Application newApplication;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationRepository applicationRepository;

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationAssetLicenceService applicationAssetLicenceService;

  @Mock
  private UserDetailService userDetailService;

  private ServiceUserDetail serviceUser;

  @BeforeEach
  void setup() {
    ApplicationConfigurationProperties applicationConfigurationProperties = new ApplicationConfigurationProperties(
        APPLICATION_NUMBER_START_VALUE);
    serviceUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(USER_WUA_ID)
        .build();

    applicationService = new ApplicationService(
        applicationRepository,
        applicationVersionRepository,
        applicationAssetService,
        applicationAssetLicenceService,
        applicationConfigurationProperties,
        userDetailService
    );
    newApplication = new Application(1, ApplicationType.PRODUCTION, Instant.now(), USER_WUA_ID, 0, null);
  }


  @Test
  void createNewApplicationForField() {
    OrganisationUnitJson organisationUnitJson = OrganisationUnitTestUtil.orgUnit1Json;
    when(applicationRepository.save(any(Application.class))).thenReturn(newApplication);

    ApplicationVersion newApplicationVersion = new ApplicationVersion(1, newApplication, 1, organisationUnitJson.organisationUnitId(),
        organisationUnitJson.name(), Instant.now(), USER_WUA_ID, ApplicationVersionStatus.IN_PROGRESS);
    when(applicationVersionRepository.save(any(ApplicationVersion.class))).thenReturn(newApplicationVersion);

    ApplicationAsset applicationAsset = ApplicationAssetTestUtil.fieldAsset1;
    when(applicationAssetService.createPrimaryAsset(newApplicationVersion, field1JsonWithOperatorAndLicences))
        .thenReturn(applicationAsset);
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    ApplicationVersion expectedApplicationVersion = applicationService.createNewApplicationForField(
        ApplicationType.PRODUCTION,
        field1JsonWithOperatorAndLicences,
        organisationUnitJson
    );

    assertApplicationVersion(newApplicationVersion, expectedApplicationVersion);

    verify(applicationAssetService, times(1)).createPrimaryAsset(
        newApplicationVersion,
        field1JsonWithOperatorAndLicences
    );

    verify(applicationAssetLicenceService, times(1))
        .createAssetLicences(applicationAsset, field1JsonWithOperatorAndLicences);
  }

  @Test
  void createNewApplicationForTerminal() {
    OrganisationUnitJson organisationUnitJson = OrganisationUnitTestUtil.orgUnit1Json;
    when(applicationRepository.save(any(Application.class))).thenReturn(newApplication);

    ApplicationVersion newApplicationVersion = new ApplicationVersion(1, newApplication, 1, organisationUnitJson.organisationUnitId(),
        organisationUnitJson.name(), Instant.now(), USER_WUA_ID, ApplicationVersionStatus.IN_PROGRESS);
    when(applicationVersionRepository.save(any(ApplicationVersion.class))).thenReturn(newApplicationVersion);
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    ApplicationVersion expectedApplicationVersion = applicationService.createNewApplicationForTerminal(
        ApplicationType.PRODUCTION,
        terminal1JsonWithOperator,
        organisationUnitJson
    );

    assertApplicationVersion(newApplicationVersion, expectedApplicationVersion);

    verify(applicationAssetService, times(1)).createPrimaryAsset(
        newApplicationVersion,
        terminal1JsonWithOperator
    );
  }

  private void assertApplicationVersion(ApplicationVersion newApplicationVersion, ApplicationVersion expectedApplicationVersion) {
    assertThat(expectedApplicationVersion.getId()).isEqualTo(newApplicationVersion.getId());
    assertThat(expectedApplicationVersion.getVersion()).isEqualTo(newApplicationVersion.getVersion());
    assertThat(expectedApplicationVersion.getPrimaryOperatorOuId())
        .isEqualTo(newApplicationVersion.getPrimaryOperatorOuId());
    assertThat(expectedApplicationVersion.getCachedPrimaryOperatorName())
        .isEqualTo(newApplicationVersion.getCachedPrimaryOperatorName());
    assertThat(expectedApplicationVersion.getCreatedByWuaId()).isEqualTo(USER_WUA_ID);
    assertThat(expectedApplicationVersion.getCreatedDateTime()).isAfterOrEqualTo(newApplicationVersion.getCreatedDateTime());
    assertThat(expectedApplicationVersion.getStatus()).isEqualTo(ApplicationVersionStatus.IN_PROGRESS);

    Application expectedApplication = expectedApplicationVersion.getApplication();
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
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    applicationService.submitApplication(applicationVersion);

    ArgumentCaptor<Application> applicationArgumentCaptor = ArgumentCaptor.forClass(Application.class);
    verify(applicationRepository, times(1)).save(applicationArgumentCaptor.capture());

    var actualApplication = applicationArgumentCaptor.getValue();

    assertThat(actualApplication.getApplicationNo()).isEqualTo(2);
    assertThat(actualApplication.getVariationNo()).isEqualTo(0);
  }

  @Test
  void submitApplicationVersion_whenAlreadySubmitted() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    var exception = Assertions.assertThrows(
        IllegalStateException.class,
        () -> applicationService.submitApplicationVersion(applicationVersion)
    );

    Assertions.assertEquals("Application with id 1 cannot be submitted", exception.getMessage());
  }
  
  @Test
  void submitApplicationVersion() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(userDetailService.getUserDetail()).thenReturn(serviceUser);

    applicationService.submitApplicationVersion(applicationVersion);

    ArgumentCaptor<ApplicationVersion> applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);
    verify(applicationVersionRepository, times(1)).save(applicationVersionArgumentCaptor.capture());

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
