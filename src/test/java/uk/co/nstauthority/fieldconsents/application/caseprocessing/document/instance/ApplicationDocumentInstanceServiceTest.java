package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.branding.BrandingTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentInstanceServiceTest {

  private static final String APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE = "APPLICATION";

  @Mock
  private ApplicationDocumentInstanceSectionViewService applicationDocumentInstanceSectionViewService;

  @Mock
  private DocumentTemplateService documentTemplateService;

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  private ApplicationDocumentInstanceService applicationDocumentInstanceService;

  @BeforeEach
  void setUp() {
    applicationDocumentInstanceService = spy(new ApplicationDocumentInstanceService(
        applicationDocumentInstanceSectionViewService,
        documentTemplateService,
        documentInstanceService,
        applicationService,
        applicationVersionService,
        applicationAssetService,
        BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES
    ));
  }

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

    doReturn(itemReference).when(applicationDocumentInstanceService).getItemReference(application);

    doReturn(applicableDocumentTemplateTypes)
        .when(applicationDocumentInstanceService)
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

    applicationDocumentInstanceService.createDocumentInstancesForApplication(application);

    verify(applicationDocumentInstanceService).createDocumentInstance(
        application,
        fieldProductionConsentDocumentTemplateDto,
        DocumentTemplateType.FIELD_PRODUCTION_CONSENT
    );
    verify(applicationDocumentInstanceService).createDocumentInstance(
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

    doReturn(itemReference).when(applicationDocumentInstanceService).getItemReference(application);

    doReturn(applicableDocumentTemplateTypes)
        .when(applicationDocumentInstanceService)
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

    applicationDocumentInstanceService.createDocumentInstancesForApplication(application);

    verify(applicationDocumentInstanceService, never()).createDocumentInstance(
        application,
        fieldProductionConsentDocumentTemplateDto,
        DocumentTemplateType.FIELD_PRODUCTION_CONSENT
    );
    verify(applicationDocumentInstanceService).createDocumentInstance(
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
        applicationDocumentInstanceService.createDocumentInstance(
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

    assertThat(applicationDocumentInstanceService.getDocumentInstanceDtos(application)).isEqualTo(documentInstanceDtos);
  }

  @Test
  void renderPdf() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var pdfRenderingOptions = PdfRenderingOptions.newBuilder().build();
    var documentInstanceSectionsSummaryView = mock(DocumentInstanceSectionsSummaryView.class);
    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});
    var applicationReference = "Application reference";

    var expectedTemplateModel = Map.of(
        "documentInstanceSectionsSummaryView", documentInstanceSectionsSummaryView,
        "previewWatermark", pdfRenderingOptions.previewWatermark(),
        "applicationReference", applicationReference,
        "customerBrandingConfigurationProperties", BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES
    );

    when(applicationDocumentInstanceSectionViewService.getDocumentInstanceSectionsSummaryView(
        applicationVersion.getApplication(),
        documentInstanceDto,
        false
    )).thenReturn(documentInstanceSectionsSummaryView);

    when(applicationService.generateApplicationReference(applicationVersion)).thenReturn(applicationReference);

    when(documentInstanceService.renderPdf(documentInstanceDto, expectedTemplateModel))
        .thenReturn(byteArrayResource);

    assertThat(applicationDocumentInstanceService.renderPdf(applicationVersion, documentInstanceDto, pdfRenderingOptions))
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

    assertThatThrownBy(() -> applicationDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
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
        .when(applicationDocumentInstanceService)
        .isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset);

    assertThat(applicationDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
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
        .when(applicationDocumentInstanceService)
        .isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset);

    assertThat(applicationDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
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

    assertThat(applicationDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
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

    assertThat(applicationDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
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

    assertThat(applicationDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
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

    assertThat(applicationDocumentInstanceService.getApplicableDocumentTemplateTypes(application))
        .containsExactly(DocumentTemplateType.TERMINAL_VENT_CONSENT);
  }

  @Test
  void isFlareCommissioningLetterApplicableForProductionApplication_primaryAssetIsTerminal() {
    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.TERMINAL);

    assertThat(applicationDocumentInstanceService.isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset))
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

    assertThat(applicationDocumentInstanceService.isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset))
        .isEqualTo(!completedApplicationExists);
  }

  @Test
  void getItemReference() {
    var application = new Application(1);

    assertThat(applicationDocumentInstanceService.getItemReference(application))
        .isEqualTo(application.getId().toString());
  }
}
