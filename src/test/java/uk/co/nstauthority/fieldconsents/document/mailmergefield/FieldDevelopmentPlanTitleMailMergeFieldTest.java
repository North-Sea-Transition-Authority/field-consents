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
  private FieldDevelopmentPlanTitleMailMergeField mailMergeField;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getMnemonic() {
    assertThat(mailMergeField.getMnemonic()).isEqualTo(MNEMONIC);
  }

  @Test
  void getDescription() {
    assertThat(mailMergeField.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  void isApplicable() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic())
        .build();

    assertThat(mailMergeField.isApplicable(documentTemplateDto)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = DocumentTemplateType.class, mode = EnumSource.Mode.EXCLUDE, names = "FIELD_PRODUCTION_CONSENT")
  void isApplicable_whenNotFieldConsent(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(mailMergeField.isApplicable(documentTemplateDto)).isFalse();
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

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto)).thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE)).thenReturn(Optional.of(field));

    assertThat(mailMergeField.resolve(documentInstanceDto)).isEqualTo(fieldDevelopmentPlanTitle);
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, mode = EnumSource.Mode.EXCLUDE, names = "FIELD")
  void resolve_primaryAssetIsNotField(AssetType assetType) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(assetType).build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto)).thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    assertThatThrownBy(() -> mailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

  @Test
  void resolve_fieldDoesNotExist() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto)).thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }
}
