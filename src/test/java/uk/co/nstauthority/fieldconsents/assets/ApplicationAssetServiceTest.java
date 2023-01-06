package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;

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
}