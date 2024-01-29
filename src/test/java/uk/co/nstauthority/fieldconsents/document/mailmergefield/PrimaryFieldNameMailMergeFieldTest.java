package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class PrimaryFieldNameMailMergeFieldTest {

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldService fieldService;

  @InjectMocks
  private PrimaryFieldNameMailMergeField primaryFieldNameMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(primaryFieldNameMailMergeField.getMnemonic()).isEqualTo("PRIMARY_FIELD_NAME");
  }

  @Test
  void getDescription() {
    assertThat(primaryFieldNameMailMergeField.getDescription())
        .isEqualTo("The name of the primary field on the application");
  }

  @Test
  void isApplicable_documentTemplateTypeIsFieldProductionConsent() {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic())
        .build();

    assertThat(primaryFieldNameMailMergeField.isApplicable(template)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = DocumentTemplateType.class, names = "FIELD_PRODUCTION_CONSENT", mode = EnumSource.Mode.EXCLUDE)
  void isApplicable_documentTemplateTypeIsNotFieldProductionConsent(DocumentTemplateType documentTemplateType) {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(primaryFieldNameMailMergeField.isApplicable(template)).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, names = "FIELD", mode = EnumSource.Mode.EXCLUDE)
  void resolve_primaryAssetTypeIsNotField(AssetType assetType) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(assetType)
        .build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);

    assertThatThrownBy(() -> primaryFieldNameMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

  @Test
  void primaryAssetTypeIsField() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(AssetType.FIELD)
        .build();

    var fieldName = "CLAIR";
    var fieldJson = new FieldJson(null, fieldName, null, null, null);

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);
    when(fieldService.getField(applicationAsset.getAssetId(), "Field lookup for application asset"))
        .thenReturn(fieldJson);

    assertThat(primaryFieldNameMailMergeField.resolve(documentInstanceDto)).isEqualTo(fieldName);
  }
}
