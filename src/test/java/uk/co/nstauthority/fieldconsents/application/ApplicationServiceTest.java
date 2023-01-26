package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import java.time.Instant;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

  private static Application newApplication;

  private static ApplicationService applicationService;

  private static ApplicationRepository applicationRepository;

  private static ApplicationVersionRepository applicationVersionRepository;

  private static ApplicationAssetService applicationAssetService;

  private static ApplicationAssetLicenceService applicationAssetLicenceService;

  @BeforeAll
  static void setup() {
    applicationRepository = mock(ApplicationRepository.class);
    applicationVersionRepository = mock(ApplicationVersionRepository.class);
    applicationAssetService = mock(ApplicationAssetService.class);
    applicationAssetLicenceService = mock(ApplicationAssetLicenceService.class);
    applicationService = new ApplicationService(
        applicationRepository,
        applicationVersionRepository,
        applicationAssetService,
        applicationAssetLicenceService);
    newApplication = new Application(1, ApplicationType.PRODUCTION, Instant.now(), 1);
  }


  @Test
  void createNewApplicationForField() {
    OrganisationUnitJson organisationUnitJson = OrganisationUnitTestUtil.orgUnit1Json;
    when(applicationRepository.save(any(Application.class))).thenReturn(newApplication);

    ApplicationVersion newApplicationVersion = new ApplicationVersion(1, newApplication, 1, organisationUnitJson.organisationUnitId(),
        organisationUnitJson.name());
    when(applicationVersionRepository.save(any(ApplicationVersion.class))).thenReturn(newApplicationVersion);

    ApplicationAsset applicationAsset = ApplicationAssetTestUtil.fieldAsset1;
    when(applicationAssetService.createPrimaryAsset(newApplicationVersion, field1JsonWithOperatorAndLicences))
        .thenReturn(applicationAsset);

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
        organisationUnitJson.name());
    when(applicationVersionRepository.save(any(ApplicationVersion.class))).thenReturn(newApplicationVersion);

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
}