package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@ExtendWith(MockitoExtension.class)
class ApplicationAssetServiceTest {

  @Mock
  private ApplicationAssetRepository applicationAssetRepository;

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @Captor
  private ArgumentCaptor<ApplicationAsset> assetArgumentCaptor;

  private ApplicationAssetService applicationAssetService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationAssetService = new ApplicationAssetService(
        fieldService,
        terminalService,
        applicationAssetRepository
    );
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void createAssetRecordForField() {
    when(fieldService.getFieldOrError(field1Json.fieldId(), null)).thenReturn(field1Json);

    applicationAssetService.createAssetRecordForField(applicationVersion, field1Json.fieldId());

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset.getApplicationVersion().getId()).isEqualTo(applicationVersion.getId());
    assertThat(capturedAsset.getAssetRole()).isEqualTo(AssetRole.PRIMARY);
    assertThat(capturedAsset.getFieldId()).isEqualTo(field1Json.fieldId());
    assertThat(capturedAsset.getCachedFieldName()).isEqualTo(field1Json.fieldName());
    assertThat(capturedAsset.getTerminalId()).isNull();
    assertThat(capturedAsset.getCachedTerminalName()).isNull();
  }

  @Test
  void createAssetRecordForTerminal() {
    when(terminalService.getTerminalOrError(terminal1Json.terminalId(), null)).thenReturn(terminal1Json);

    applicationAssetService.createAssetRecordForTerminal(applicationVersion, terminal1Json.terminalId());

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset.getApplicationVersion().getId()).isEqualTo(applicationVersion.getId());
    assertThat(capturedAsset.getAssetRole()).isEqualTo(AssetRole.PRIMARY);
    assertThat(capturedAsset.getFieldId()).isNull();
    assertThat(capturedAsset.getCachedFieldName()).isNull();
    assertThat(capturedAsset.getTerminalId()).isEqualTo(terminal1Json.terminalId());
    assertThat(capturedAsset.getCachedTerminalName()).isEqualTo(terminal1Json.terminalName());
  }

  private ApplicationAsset getStubPrimaryApplicationAsset(ApplicationVersion applicationVersion) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setAssetRole(AssetRole.PRIMARY);
    return applicationAsset;
  }

  @Test
  void getPrimaryApplicationAsset_exists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    when(applicationAssetRepository.findByApplicationVersionAndAssetRole(applicationVersion, AssetRole.PRIMARY))
        .thenReturn(Optional.of(applicationAsset));
    assertThat(applicationAssetService.getPrimaryApplicationAsset(applicationVersion))
        .isEqualTo(applicationAsset);
  }

  @Test
  void getPrimaryApplicationAsset_noneExists() {
    when(applicationAssetRepository.findByApplicationVersionAndAssetRole(applicationVersion, AssetRole.PRIMARY))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationAssetService.getPrimaryApplicationAsset(applicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Primary application asset not found for application version id %s"
            .formatted(applicationVersion.getId()));
  }

  @Test
  void getAssetJsonForApplicationAsset_noAssetIdsExists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    assertThatThrownBy(() -> applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Field and terminal ids not found for application asset id %s"
            .formatted(applicationAsset.getId()));
  }

  @Test
  void getAssetJsonForApplicationAsset_fieldExists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setFieldId(field1.getFieldId());
    when(fieldService.getField(eq(field1.getFieldId()), any())).thenReturn(Optional.of(field1Json));

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(new AssetJson(field1.getFieldId(), field1.getFieldName(), AssetType.FIELD));
  }

  @Test
  void getAssetJsonForApplicationAsset_fieldIdExistsButNameLookupFails() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setFieldId(field1.getFieldId());
    applicationAsset.setCachedFieldName(field1.getFieldName());

    when(fieldService.getField(eq(field1.getFieldId()), any())).thenReturn(Optional.empty());

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(new AssetJson(field1.getFieldId(), field1.getFieldName(), AssetType.FIELD));
  }

  @Test
  void getAssetJsonForApplicationAsset_terminalExists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setTerminalId(terminal1.getTerminalId());
    when(terminalService.getTerminal(eq(terminal1.getTerminalId()), any()))
        .thenReturn(Optional.of(terminal1Json));

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(new AssetJson(terminal1.getTerminalId(), terminal1.getTerminalName(), AssetType.TERMINAL));
  }

  @Test
  void getAssetJsonForApplicationAsset_terminalIdExistsButNameLookupFails() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setTerminalId(terminal1.getTerminalId());
    applicationAsset.setCachedTerminalName(terminal1.getTerminalName());
    when(terminalService.getTerminal(eq(terminal1.getTerminalId()), any()))
        .thenReturn(Optional.empty());

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(new AssetJson(terminal1.getTerminalId(), terminal1.getTerminalName(), AssetType.TERMINAL));
  }
}