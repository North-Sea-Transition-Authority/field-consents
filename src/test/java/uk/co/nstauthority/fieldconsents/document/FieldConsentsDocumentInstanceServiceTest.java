package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceServiceTest {

  private static final String APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE = "APPLICATION";

  @Mock
  private FieldConsentsDocumentInstanceSectionControllerHelperService fieldConsentsDocumentInstanceSectionControllerHelperService;

  @Mock
  private DocumentTemplateService documentTemplateService;

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @InjectMocks
  @Spy
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @Test
  void createDocumentInstancesForApplication() {
    var application = new Application();

    var itemReference = "Test/item/reference";

    var applicableDocumentTemplateTypes = List.of(
        DocumentTemplateType.FIELD_PRODUCTION_CONSENT,
        DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER
    );

    var fieldProductionConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var flareAndCommissioningLetterDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    doReturn(itemReference).when(fieldConsentsDocumentInstanceService).getItemReference(application);

    doReturn(applicableDocumentTemplateTypes)
        .when(fieldConsentsDocumentInstanceService)
        .getApplicableDocumentTemplateTypes(application);

    when(
        documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(
            DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic()
        )
    ).thenReturn(fieldProductionConsentDocumentTemplateDto);
    when(
        documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
            itemReference,
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
            fieldProductionConsentDocumentTemplateDto
        )
    ).thenReturn(Optional.empty());

    when(
        documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(
            DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER.getMnemonic()
        )
    ).thenReturn(flareAndCommissioningLetterDocumentTemplateDto);
    when(
        documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
            itemReference,
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
            flareAndCommissioningLetterDocumentTemplateDto
        )
    ).thenReturn(Optional.empty());

    fieldConsentsDocumentInstanceService.createDocumentInstancesForApplication(application);

    verify(fieldConsentsDocumentInstanceService).createDocumentInstance(
        application,
        fieldProductionConsentDocumentTemplateDto,
        DocumentTemplateType.FIELD_PRODUCTION_CONSENT
    );
    verify(fieldConsentsDocumentInstanceService).createDocumentInstance(
        application,
        flareAndCommissioningLetterDocumentTemplateDto,
        DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER
    );
  }

  @Test
  void createDocumentInstancesForApplication_documentInstanceAlreadyExists() {
    var application = new Application();

    var itemReference = "Test/item/reference";

    var applicableDocumentTemplateTypes = List.of(
        DocumentTemplateType.FIELD_PRODUCTION_CONSENT,
        DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER
    );

    var fieldProductionConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var fieldProductionConsentDocumentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var flareAndCommissioningLetterDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    doReturn(itemReference).when(fieldConsentsDocumentInstanceService).getItemReference(application);

    doReturn(applicableDocumentTemplateTypes)
        .when(fieldConsentsDocumentInstanceService)
        .getApplicableDocumentTemplateTypes(application);

    when(
        documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(
            DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic()
        )
    ).thenReturn(fieldProductionConsentDocumentTemplateDto);
    when(
        documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
            itemReference,
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
            fieldProductionConsentDocumentTemplateDto
        )
    ).thenReturn(Optional.of(fieldProductionConsentDocumentInstanceDto));

    when(
        documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(
            DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER.getMnemonic()
        )
    ).thenReturn(flareAndCommissioningLetterDocumentTemplateDto);
    when(
        documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
            itemReference,
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
            flareAndCommissioningLetterDocumentTemplateDto
        )
    ).thenReturn(Optional.empty());

    fieldConsentsDocumentInstanceService.createDocumentInstancesForApplication(application);

    verify(fieldConsentsDocumentInstanceService, never()).createDocumentInstance(
        application,
        fieldProductionConsentDocumentTemplateDto,
        DocumentTemplateType.FIELD_PRODUCTION_CONSENT
    );
    verify(fieldConsentsDocumentInstanceService).createDocumentInstance(
        application,
        flareAndCommissioningLetterDocumentTemplateDto,
        DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER
    );
  }

  @Test
  void createDocumentInstance() {
    var application = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION).getApplication();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentTemplateType = DocumentTemplateType.FIELD_PRODUCTION_CONSENT;
    var itemReference = application.getId().toString();

    when(
        documentInstanceService.createDocumentInstance(
            itemReference,
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
            documentTemplateDto.title(),
            documentTemplateType.getDocumentInstanceDescription(),
            documentTemplateDto
        )
    ).thenReturn(documentInstanceDto);

    assertThat(
        fieldConsentsDocumentInstanceService.createDocumentInstance(
            application,
            documentTemplateDto,
            documentTemplateType
        )
    ).isEqualTo(documentInstanceDto);
  }

  @Test
  void getDocumentInstanceDtos() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.VENT);
    var itemReference = application.getId().toString();

    var documentInstanceDtos = List.of(
        DocumentInstanceDtoTestUtil.builder().build(),
        DocumentInstanceDtoTestUtil.builder().build()
    );

    when(documentInstanceService.getDocumentInstanceDtosByItemReference(itemReference))
        .thenReturn(documentInstanceDtos);

    assertThat(fieldConsentsDocumentInstanceService.getDocumentInstanceDtos(application)).isEqualTo(documentInstanceDtos);
  }

  @Test
  void renderPdf() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var pdfRenderingOptions = PdfRenderingOptions.newBuilder().build();
    var documentInstanceSectionsSummaryView = mock(DocumentInstanceSectionsSummaryView.class);
    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});
    var expectedTemplateModel = Map.of(
        "documentInstanceSectionsSummaryView", documentInstanceSectionsSummaryView,
        "previewWatermark", pdfRenderingOptions.previewWatermark()
    );

    when(
        fieldConsentsDocumentInstanceSectionControllerHelperService.getDocumentInstanceSectionsSummaryView(
            application,
            documentInstanceDto,
            false
        )
    ).thenReturn(documentInstanceSectionsSummaryView);

    when(documentInstanceService.renderPdf(documentInstanceDto, expectedTemplateModel))
        .thenReturn(byteArrayResource);

    assertThat(fieldConsentsDocumentInstanceService.renderPdf(application, documentInstanceDto, pdfRenderingOptions))
        .isEqualTo(byteArrayResource);
  }

  @Test
  void getApplicableDocumentTemplateTypes_applicationTypeIsProductionAndPrimaryAssetTypeIsTerminal() {
    var application = new Application();
    application.setType(ApplicationType.PRODUCTION);

    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset(1);
    primaryAsset.setAssetType(AssetType.TERMINAL);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    assertThatThrownBy(() -> fieldConsentsDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Primary asset 1 is not field [asset type: TERMINAL]");
  }

  @Test
  void getApplicableDocumentTemplateTypes_applicationTypeIsProductionAndFlareCommissioningLetterIsApplicable() {
    var application = new Application();
    application.setType(ApplicationType.PRODUCTION);

    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.FIELD);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    doReturn(true)
        .when(fieldConsentsDocumentInstanceService)
        .isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset);

    assertThat(fieldConsentsDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
        .containsExactly(DocumentTemplateType.FIELD_PRODUCTION_CONSENT, DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER);
  }

  @Test
  void getApplicableDocumentTemplateTypes_applicationTypeIsProductionAndFlareCommissioningLetterIsNotApplicable() {
    var application = new Application();
    application.setType(ApplicationType.PRODUCTION);

    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.FIELD);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    doReturn(false)
        .when(fieldConsentsDocumentInstanceService)
        .isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset);

    assertThat(fieldConsentsDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
        .containsExactly(DocumentTemplateType.FIELD_PRODUCTION_CONSENT);
  }

  @Test
  void getApplicableDocumentTemplateTypes_applicationTypeIsFlareAndPrimaryAssetIsField() {
    var application = new Application();
    application.setType(ApplicationType.FLARE);

    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.FIELD);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    assertThat(fieldConsentsDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
        .containsExactly(DocumentTemplateType.FIELD_FLARE_CONSENT);
  }

  @Test
  void getApplicableDocumentTemplateTypes_applicationTypeIsFlareAndPrimaryAssetIsTerminal() {
    var application = new Application();
    application.setType(ApplicationType.FLARE);

    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.TERMINAL);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    assertThat(fieldConsentsDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
        .containsExactly(DocumentTemplateType.TERMINAL_FLARE_CONSENT);
  }

  @Test
  void getApplicableDocumentTemplateTypes_applicationTypeIsVentAndPrimaryAssetIsField() {
    var application = new Application();
    application.setType(ApplicationType.VENT);

    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.FIELD);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    assertThat(fieldConsentsDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
        .containsExactly(DocumentTemplateType.FIELD_VENT_CONSENT);
  }

  @Test
  void getApplicableDocumentTemplateTypes_applicationTypeIsVentAndPrimaryAssetIsTerminal() {
    var application = new Application();
    application.setType(ApplicationType.VENT);

    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.TERMINAL);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    assertThat(fieldConsentsDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
        .containsExactly(DocumentTemplateType.TERMINAL_VENT_CONSENT);
  }

  @Test
  void isFlareCommissioningLetterApplicableForProductionApplication_primaryAssetIsTerminal() {
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.TERMINAL);

    assertThat(fieldConsentsDocumentInstanceService.isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset))
        .isFalse();
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void isFlareCommissioningLetterApplicableForProductionApplication_primaryAssetIsField(boolean completedApplicationExists) {
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.FIELD);

    var fieldId = 1;
    primaryAsset.setAssetId(fieldId);

    when(applicationAssetService.completedProductionApplicationExistsWithPrimaryField(fieldId))
        .thenReturn(completedApplicationExists);

    assertThat(fieldConsentsDocumentInstanceService.isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset))
        .isEqualTo(!completedApplicationExists);
  }

  @Test
  void getItemReference() {
    var application = new Application(1);

    assertThat(fieldConsentsDocumentInstanceService.getItemReference(application))
        .isEqualTo(application.getId().toString());
  }
}
