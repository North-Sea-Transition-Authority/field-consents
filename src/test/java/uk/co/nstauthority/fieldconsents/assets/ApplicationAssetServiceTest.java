package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.ApplicationAssetTestUtil.assets;
import static uk.co.nstauthority.fieldconsents.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.assets.ApplicationAssetTestUtil.fieldAsset2;
import static uk.co.nstauthority.fieldconsents.assets.ApplicationAssetTestUtil.fieldAsset3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import java.util.Collections;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
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
  void createAssetRecordForPrimaryField() {
    applicationAssetService.createAssetRecordForPrimaryField(applicationVersion, field1JsonWithOperator);

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset.getApplicationVersion().getId()).isEqualTo(applicationVersion.getId());
    assertThat(capturedAsset.getAssetRole()).isEqualTo(AssetRole.PRIMARY);
    assertThat(capturedAsset.getFieldId()).isEqualTo(field1JsonWithOperator.fieldId());
    assertThat(capturedAsset.getCachedFieldName()).isEqualTo(field1JsonWithOperator.fieldName());
    assertThat(capturedAsset.getAssetOperatorOuId()).isEqualTo(field1JsonWithOperator.operatorOuId());
    assertThat(capturedAsset.getCachedAssetOperatorName()).isEqualTo(field1JsonWithOperator.operatorName());
    assertThat(capturedAsset.getTerminalId()).isNull();
    assertThat(capturedAsset.getCachedTerminalName()).isNull();
  }

  @Test
  void createAssetRecordForTerminal() {
    applicationAssetService.createAssetRecordForTerminal(applicationVersion, terminal1JsonWithOperator);

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset.getApplicationVersion().getId()).isEqualTo(applicationVersion.getId());
    assertThat(capturedAsset.getAssetRole()).isEqualTo(AssetRole.PRIMARY);
    assertThat(capturedAsset.getFieldId()).isNull();
    assertThat(capturedAsset.getCachedFieldName()).isNull();
    assertThat(capturedAsset.getAssetOperatorOuId()).isEqualTo(terminal1JsonWithOperator.operatorOuId());
    assertThat(capturedAsset.getCachedAssetOperatorName()).isEqualTo(terminal1JsonWithOperator.operatorName());
    assertThat(capturedAsset.getTerminalId()).isEqualTo(terminal1JsonWithOperator.terminalId());
    assertThat(capturedAsset.getCachedTerminalName()).isEqualTo(terminal1JsonWithOperator.terminalName());
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
    applicationAsset.setFieldId(field1WithOperator.getFieldId());
    when(fieldService.findField(eq(field1WithOperator.getFieldId()), any())).thenReturn(Optional.of(field1Json));

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(new AssetJson(field1WithOperator.getFieldId(), field1WithOperator.getFieldName(), AssetType.FIELD));
  }

  @Test
  void getAssetJsonForApplicationAsset_fieldIdExistsButNameLookupFails() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setFieldId(field1WithOperator.getFieldId());
    applicationAsset.setCachedFieldName(field1WithOperator.getFieldName());

    when(fieldService.findField(eq(field1WithOperator.getFieldId()), any())).thenReturn(Optional.empty());

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(new AssetJson(field1WithOperator.getFieldId(), field1WithOperator.getFieldName(), AssetType.FIELD));
  }

  @Test
  void getAssetJsonForApplicationAsset_terminalExists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setTerminalId(terminal1.getTerminalId());
    when(terminalService.findTerminal(eq(terminal1.getTerminalId()), any()))
        .thenReturn(Optional.of(terminal1Json));

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(new AssetJson(terminal1.getTerminalId(), terminal1.getTerminalName(), AssetType.TERMINAL));
  }

  @Test
  void getAssetJsonForApplicationAsset_terminalIdExistsButNameLookupFails() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setTerminalId(terminal1.getTerminalId());
    applicationAsset.setCachedTerminalName(terminal1.getTerminalName());
    when(terminalService.findTerminal(eq(terminal1.getTerminalId()), any()))
        .thenReturn(Optional.empty());

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(new AssetJson(terminal1.getTerminalId(), terminal1.getTerminalName(), AssetType.TERMINAL));
  }

  @Test
  void getAdditionalAssetsForApplicationVersion_noAssets() {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(Collections.emptyList());

    assertThat(applicationAssetService.getAdditionalAssetsForApplicationVersion(applicationVersion)).isEmpty();
  }

  @Test
  void getAdditionalAssetsForApplicationVersion_withAssets() {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(assets);

    assertThat(applicationAssetService.getAdditionalAssetsForApplicationVersion(applicationVersion))
        .containsExactly(
            fieldAsset1,
            fieldAsset2,
            fieldAsset3);
  }

  @Test
  void additionalAssetsExistForApplicationVersion_doNotExist() {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(Collections.emptyList());

    assertThat(applicationAssetService.additionalAssetsExistForApplicationVersion(applicationVersion)).isFalse();
  }

  @Test
  void additionalAssetsExistForApplicationVersion_exist() {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(assets);

    assertThat(applicationAssetService.additionalAssetsExistForApplicationVersion(applicationVersion)).isTrue();
  }

  @Test
  void saveAdditionalAsset_withTerminalJson() {
    AssetJson terminalAssetJson = AssetJson.from(terminal1JsonWithOperator);
    assertThatThrownBy(() -> applicationAssetService.saveAdditionalAsset(applicationVersion, terminalAssetJson))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(
            "Secondary asset is not allowed on terminal asset with id " + terminal1JsonWithOperator.terminalId());
  }

  @Test
  void saveAdditionalAsset_withNoOperatorFound() {
    when(fieldService
        .getFieldWithOperator(AssetJson.from(field1Json).assetId(), "Lookup field prior to creating a field application"))
        .thenReturn(field1Json);

    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(assets);

    AssetJson fieldAssetJson = AssetJson.from(field1Json);
    assertThatThrownBy(() -> applicationAssetService.saveAdditionalAsset(applicationVersion, fieldAssetJson))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining(String.format("No operator was found for field %s with id %s.",  field1Json.fieldName(),  field1Json.fieldId()));
  }

  @Test
  void saveAdditionalAsset_withOperator() {
    when(fieldService
        .getFieldWithOperator(AssetJson.from(field2JsonWithOperator).assetId(), "Lookup field prior to creating a field application"))
        .thenReturn(field2JsonWithOperator);

    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(assets);

    applicationAssetService.saveAdditionalAsset(applicationVersion, AssetJson.from(field2JsonWithOperator));

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset.getApplicationVersion().getId()).isEqualTo(applicationVersion.getId());
    assertThat(capturedAsset.getAssetRole()).isEqualTo(AssetRole.SECONDARY);
    assertThat(capturedAsset.getAssetNo()).isEqualTo(assets.size() + 1);
    assertThat(capturedAsset.getFieldId()).isEqualTo(field2JsonWithOperator.fieldId());
    assertThat(capturedAsset.getCachedFieldName()).isEqualTo(field2JsonWithOperator.fieldName());
    assertThat(capturedAsset.getAssetOperatorOuId()).isEqualTo(field2JsonWithOperator.operatorOuId());
    assertThat(capturedAsset.getCachedAssetOperatorName()).isEqualTo(field2JsonWithOperator.operatorName());
    assertThat(capturedAsset.getTerminalId()).isNull();
    assertThat(capturedAsset.getCachedTerminalName()).isNull();
  }

  @Test
  void getAsset_noAssetFound() {
    when(applicationAssetRepository.findByApplicationVersionAndAssetNo(applicationVersion, 1)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationAssetService.getAsset(applicationVersion, 1))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining(String.format("Asset with application version id %s and asset no %s not found", applicationVersion.getId(), 1));
  }

  @Test
  void getAsset_assetExists() {
    when(applicationAssetRepository.findByApplicationVersionAndAssetNo(applicationVersion, 1))
        .thenReturn(Optional.of(fieldAsset1));

    assertThat(applicationAssetService.getAsset(applicationVersion, 1)).isEqualTo(fieldAsset1);
  }

  @Test
  void deleteAsset() {
    applicationAssetService.deleteAsset(fieldAsset1);

    verify(applicationAssetRepository, times(1)).delete(fieldAsset1);
  }
}