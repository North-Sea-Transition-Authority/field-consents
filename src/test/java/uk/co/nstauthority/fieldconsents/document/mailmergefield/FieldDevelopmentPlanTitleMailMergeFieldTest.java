package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanTitleMailMergeField.DESCRIPTION;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanTitleMailMergeField.MNEMONIC;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanTitleMailMergeField.QUERY;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanTitleMailMergeField.REQUEST_PURPOSE;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldDevelopmentPlan;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class FieldDevelopmentPlanTitleMailMergeFieldTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private FieldApi fieldApi;

  @InjectMocks
  private FieldDevelopmentPlanTitleMailMergeField fieldDevelopmentPlanTitleMailMergeField;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getMnemonic() {
    assertThat(fieldDevelopmentPlanTitleMailMergeField.getMnemonic()).isEqualTo(MNEMONIC);
  }

  @Test
  void getDescription() {
    assertThat(fieldDevelopmentPlanTitleMailMergeField.getDescription()).isEqualTo(DESCRIPTION);
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(fieldDevelopmentPlanTitleMailMergeField.isApplicable(documentTemplateDto))
        .isEqualTo(documentTemplateType.isApplicableToFieldApplications());
  }

  @Test
  void resolve() {
    var fieldDevelopmentPlanTitle = "Example Field Development Plan";
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();

    var field = Field.newBuilder()
        .fieldDevelopmentPlan(FieldDevelopmentPlan.newBuilder()
            .title(fieldDevelopmentPlanTitle)
            .build())
        .build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(fieldDevelopmentPlanTitleMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(fieldDevelopmentPlanTitle));
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, mode = EnumSource.Mode.EXCLUDE, names = "FIELD")
  void resolve_primaryAssetIsNotField(AssetType assetType) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(assetType).build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    assertThatThrownBy(() -> fieldDevelopmentPlanTitleMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

  @Test
  void resolve_fieldDoesNotExist() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> fieldDevelopmentPlanTitleMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class)
        .hasMessage("Field not found for primary application asset [%s]".formatted(primaryApplicationAsset.getId()));
  }

  @Test
  void resolve_fieldDoesNotHaveFieldDevelopmentPlan() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();

    var field = Field.newBuilder()
        .fieldName("field name")
        .build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(fieldDevelopmentPlanTitleMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.error(
            "Mail merge field %s is not valid. Field Development Plan does not exist for field %s"
                .formatted(MNEMONIC, field.getFieldName())
        ));
  }

  @Test
  void resolve_fieldDevelopmentPlan_missingTitle() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();

    var field = Field.newBuilder()
        .fieldName("field name")
        .fieldDevelopmentPlan(FieldDevelopmentPlan.newBuilder().build())
        .build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(fieldDevelopmentPlanTitleMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.error(
            "Mail merge field %s is not valid. Field Development Plan for field %s does not have a title"
                .formatted(MNEMONIC, field.getFieldName())
        ));
  }

}
