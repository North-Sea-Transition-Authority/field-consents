package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanAddendumListMailMergeField.MNEMONIC;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanAddendumListMailMergeField.QUERY;
import static uk.co.nstauthority.fieldconsents.document.mailmergefield.FieldDevelopmentPlanAddendumListMailMergeField.REQUEST_PURPOSE;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldDevelopmentPlan;
import uk.co.fivium.energyportalapi.generated.types.FieldDevelopmentPlanAddendum;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.util.StringUtil;

@ExtendWith(MockitoExtension.class)
class FieldDevelopmentPlanAddendumListMailMergeFieldTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @Mock
  private FieldApi fieldApi;

  @InjectMocks
  private FieldDevelopmentPlanAddendumListMailMergeField mailMergeField;

  private ApplicationVersion applicationVersion;

  private DocumentInstanceDto documentInstanceDto;

  private ApplicationAsset primaryApplicationAsset;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();
  }

  @Test
  void getMnemonic() {
    assertThat(mailMergeField.getMnemonic()).isEqualTo("FIELD_DEVELOPMENT_PLAN_ADDENDUM_LIST");
  }

  @Test
  void getDescription() {
    assertThat(mailMergeField.getDescription()).isEqualTo("The list of addendums for the Field Development Plan on the primary field.");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(mailMergeField.isApplicable(documentTemplateDto))
        .isEqualTo(documentTemplateType.isApplicableToFieldApplications());
  }

  @Test
  void resolve() {
    var field = Field.newBuilder()
        .fieldDevelopmentPlan(FieldDevelopmentPlan.newBuilder()
            .addendums(List.of(
                FieldDevelopmentPlanAddendum.newBuilder()
                    .title("addendum 1")
                    .date(LocalDate.of(2024, 1, 1))
                    .build(),
                FieldDevelopmentPlanAddendum.newBuilder()
                    .title("addendum 2")
                    .date(LocalDate.of(2024, 1, 2))
                    .build(),
                FieldDevelopmentPlanAddendum.newBuilder()
                    .title("addendum 3")
                    .date(LocalDate.of(2024, 1, 3))
                    .build()
            ))
            .build())
        .build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(primaryApplicationAsset);

    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(mailMergeField.resolve(documentInstanceDto)).isEqualTo(DocumentMailMergeFieldResolveResult.success(
        StringUtil.formatStringList(List.of(
            "addendum 1 dated 1 January 2024",
            "addendum 2 dated 2 January 2024",
            "addendum 3 dated 3 January 2024"
        ))
    ));
  }

  @Test
  void resolve_missingAddendumTitle() {
    var field = Field.newBuilder()
        .fieldDevelopmentPlan(FieldDevelopmentPlan.newBuilder()
            .addendums(List.of(
                FieldDevelopmentPlanAddendum.newBuilder()
                    // no title...
                    .date(LocalDate.of(2024, 1, 1))
                    .build(),
                FieldDevelopmentPlanAddendum.newBuilder()
                    .title("addendum 2")
                    .date(LocalDate.of(2024, 1, 2))
                    .build(),
                FieldDevelopmentPlanAddendum.newBuilder()
                    .title("addendum 3")
                    .date(LocalDate.of(2024, 1, 3))
                    .build()
            ))
            .build())
        .build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(primaryApplicationAsset);

    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(mailMergeField.resolve(documentInstanceDto)).isEqualTo(DocumentMailMergeFieldResolveResult.error(
        "Mail merge field %s is not valid. Field Development Plan Addendum is missing a date or a title".formatted(MNEMONIC)
    ));
  }

  @Test
  void resolve_missingAddendumDate() {
    var field = Field.newBuilder()
        .fieldDevelopmentPlan(FieldDevelopmentPlan.newBuilder()
            .addendums(List.of(
                FieldDevelopmentPlanAddendum.newBuilder()
                    .title("addendum 1")
                    // no date...
                    .build(),
                FieldDevelopmentPlanAddendum.newBuilder()
                    .title("addendum 2")
                    .date(LocalDate.of(2024, 1, 2))
                    .build(),
                FieldDevelopmentPlanAddendum.newBuilder()
                    .title("addendum 3")
                    .date(LocalDate.of(2024, 1, 3))
                    .build()
            ))
            .build())
        .build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(primaryApplicationAsset);

    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(mailMergeField.resolve(documentInstanceDto)).isEqualTo(DocumentMailMergeFieldResolveResult.error(
        "Mail merge field %s is not valid. Field Development Plan Addendum is missing a date or a title".formatted(MNEMONIC)
    ));
  }

  @Test
  void resolve_nullAddendums() {
    var field = Field.newBuilder()
        .fieldDevelopmentPlan(FieldDevelopmentPlan.newBuilder().build())
        .build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(primaryApplicationAsset);

    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(mailMergeField.resolve(documentInstanceDto)).isEqualTo(DocumentMailMergeFieldResolveResult.success(""));
  }

  @Test
  void resolve_emptyAddendums() {
    var field = Field.newBuilder()
        .fieldDevelopmentPlan(FieldDevelopmentPlan.newBuilder().addendums(List.of()).build())
        .build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(primaryApplicationAsset);

    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE))
        .thenReturn(Optional.of(field));

    assertThat(mailMergeField.resolve(documentInstanceDto)).isEqualTo(DocumentMailMergeFieldResolveResult.success(""));
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, mode = EnumSource.Mode.EXCLUDE, names = "FIELD")
  void resolve_primaryAssetIsNotField(AssetType assetType) {
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(assetType).build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    assertThatThrownBy(() -> mailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

  @Test
  void resolve_fieldDoesNotExist() {
    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto)).thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> mailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class)
        .hasMessage("Field not found for primary application asset [%s]".formatted(primaryApplicationAsset.getId()));
  }

  @Test
  void resolve_fieldDoesNotHaveFieldDevelopmentPlan() {
    var field = Field.newBuilder().fieldName("name").build();

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto)).thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);
    when(fieldApi.findFieldById(primaryApplicationAsset.getAssetId(), QUERY, REQUEST_PURPOSE)).thenReturn(Optional.of(field));

    assertThat(mailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.error(
            "Mail merge field %s is not valid. Field Development Plan does not exist for field %s"
                .formatted(MNEMONIC, field.getFieldName())
        ));
  }

}