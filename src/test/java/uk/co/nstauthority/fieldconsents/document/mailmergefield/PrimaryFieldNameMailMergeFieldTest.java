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
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class PrimaryFieldNameMailMergeFieldTest {

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

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

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(primaryFieldNameMailMergeField.isApplicable(template))
        .isEqualTo(documentTemplateType == DocumentTemplateType.FIELD_PRODUCTION_CONSENT
            || documentTemplateType == DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER);
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, names = "FIELD", mode = EnumSource.Mode.EXCLUDE)
  void resolve_primaryAssetTypeIsNotField(AssetType assetType) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationAsset = ApplicationAssetTestUtil.newBuilder()
        .withAssetType(assetType)
        .build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
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

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(applicationAsset);
    when(fieldService.getField(applicationAsset.getAssetId(), "Field lookup for PRIMARY_FIELD_NAME mail merge field"))
        .thenReturn(fieldJson);

    assertThat(primaryFieldNameMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(fieldName));
  }
}
