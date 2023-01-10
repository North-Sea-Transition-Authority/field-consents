package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;

import java.time.Instant;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

  private static Application newApplication;

  private static ApplicationService applicationService;

  private static ApplicationRepository applicationRepository;

  private static ApplicationVersionRepository applicationVersionRepository;

  private static ApplicationAssetService applicationAssetService;

  @BeforeAll
  static void setup() {
    applicationRepository = mock(ApplicationRepository.class);
    applicationVersionRepository = mock(ApplicationVersionRepository.class);
    applicationAssetService = mock(ApplicationAssetService.class);
    applicationService = new ApplicationService(
        applicationRepository,
        applicationVersionRepository,
        applicationAssetService
    );
    newApplication = new Application(1, ApplicationType.PRODUCTION, Instant.now(), 1);
  }


  @Test
  void createNewApplicationForField() {
    OrganisationUnitJson organisationUnitJson = new OrganisationUnitJson(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID,
            ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME);
    when(applicationRepository.save(any(Application.class))).thenReturn(newApplication);

    ApplicationVersion newApplicationVersion = new ApplicationVersion(1, newApplication, 1, organisationUnitJson.organisationUnitId(),
        organisationUnitJson.name());
    when(applicationVersionRepository.save(any(ApplicationVersion.class))).thenReturn(newApplicationVersion);

    ApplicationVersion expectedApplicationVersion = applicationService.createNewApplicationForField(
        ApplicationType.PRODUCTION,
        field1Json.fieldId(),
        organisationUnitJson
    );

    assertApplicationVersion(newApplicationVersion, expectedApplicationVersion);

    verify(applicationAssetService, times(1)).createAssetRecordForField(newApplicationVersion, field1Json.fieldId());
  }

  @Test
  void createNewApplicationForTerminal() {
    OrganisationUnitJson organisationUnitJson = new OrganisationUnitJson(ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID,
        ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME);
    when(applicationRepository.save(any(Application.class))).thenReturn(newApplication);

    ApplicationVersion newApplicationVersion = new ApplicationVersion(1, newApplication, 1, organisationUnitJson.organisationUnitId(),
        organisationUnitJson.name());
    when(applicationVersionRepository.save(any(ApplicationVersion.class))).thenReturn(newApplicationVersion);

    ApplicationVersion expectedApplicationVersion = applicationService.createNewApplicationForTerminal(
        ApplicationType.PRODUCTION,
        terminal1Json.terminalId(),
        organisationUnitJson
    );

    assertApplicationVersion(newApplicationVersion, expectedApplicationVersion);

    verify(applicationAssetService, times(1)).createAssetRecordForTerminal(newApplicationVersion, terminal1Json.terminalId());
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