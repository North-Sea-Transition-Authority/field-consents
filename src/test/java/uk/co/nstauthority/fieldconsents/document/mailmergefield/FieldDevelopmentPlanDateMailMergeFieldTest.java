package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanDateMailMergeField.DESCRIPTION;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanDateMailMergeField.MNEMONIC;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanDateMailMergeField.QUERY;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanDateMailMergeField.REQUEST_PURPOSE;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class FieldDevelopmentPlanDateMailMergeFieldTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private FieldApi fieldApi;

  @InjectMocks
  private FieldDevelopmentPlanDateMailMergeField fieldDevelopmentPlanDateMailMergeField;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getMnemonic() {
    assertThat(fieldDevelopmentPlanDateMailMergeField.getMnemonic()).isEqualTo(MNEMONIC);
  }

  @Test
  void getDescription() {
    assertThat(fieldDevelopmentPlanDateMailMergeField.getDescription()).isEqualTo(DESCRIPTION);
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(fieldDevelopmentPlanDateMailMergeField.isApplicable(template))
        .isEqualTo(DocumentTemplateType.isField(documentTemplateType));
  }

  @Test
  void resolve() {
    var fieldDevelopmentPlanDate = LocalDate.parse("2024-01-01");
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();

    var field = Field.newBuilder()
        .fieldDevelopmentPlan(FieldDevelopmentPlan.newBuilder()
            .date(fieldDevelopmentPlanDate)
            .build())
        .build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(fieldDevelopmentPlanDateMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DateUtils.format(fieldDevelopmentPlanDate, DateUtils.LONG_DATE));
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, mode = EnumSource.Mode.EXCLUDE, names = "FIELD")
  void resolve_primaryAssetIsNotField(AssetType assetType) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(assetType).build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    assertThatThrownBy(() -> fieldDevelopmentPlanDateMailMergeField.resolve(documentInstanceDto))
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

    assertThatThrownBy(() -> fieldDevelopmentPlanDateMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

}
