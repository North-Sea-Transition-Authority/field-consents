package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceServiceTest {

  private static final String APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE = "APPLICATION";

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private DocumentTemplateService documentTemplateService;

  @Mock
  private FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @InjectMocks
  @Spy
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @Test
  void createDocumentInstancesForApplication_documentInstancesAlreadyExist() {
    var application = new Application();

    var consentDocumentType = DocumentTemplateType.FIELD_PRODUCTION_CONSENT;
    var itemReference = "Test/item/reference";

    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var existingDocumentInstance = DocumentInstanceDtoTestUtil.builder().build();

    doReturn(consentDocumentType).when(fieldConsentsDocumentInstanceService).getConsentDocumentType(application);
    doReturn(itemReference).when(fieldConsentsDocumentInstanceService).getItemReference(application);
    when(documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(consentDocumentType.getMnemonic()))
        .thenReturn(documentTemplateDto);
    when(
        documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
            itemReference,
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
            documentTemplateDto
        )
    ).thenReturn(Optional.of(existingDocumentInstance));

    fieldConsentsDocumentInstanceService.createDocumentInstancesForApplication(application);

    verify(fieldConsentsDocumentInstanceService, never()).createDocumentInstance(any(), any(), any());
  }

  @Test
  void createDocumentInstancesForApplication() {
    var application = new Application();

    var consentDocumentType = DocumentTemplateType.FIELD_PRODUCTION_CONSENT;
    var itemReference = "Test/item/reference";

    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    doReturn(consentDocumentType).when(fieldConsentsDocumentInstanceService).getConsentDocumentType(application);
    doReturn(itemReference).when(fieldConsentsDocumentInstanceService).getItemReference(application);
    when(documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(consentDocumentType.getMnemonic()))
        .thenReturn(documentTemplateDto);
    when(
        documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
            itemReference,
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
            documentTemplateDto
        )
    ).thenReturn(Optional.empty());

    fieldConsentsDocumentInstanceService.createDocumentInstancesForApplication(application);

    verify(fieldConsentsDocumentInstanceService).createDocumentInstance(
        application,
        documentTemplateDto,
        consentDocumentType
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
  void getDocumentInstanceSummaryViews() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    var application = applicationVersion.getApplication();
    var itemReference = application.getId().toString();

    var documentTemplateDto1 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(1).build();
    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto1).build();

    var documentTemplateDto2 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(2).build();
    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto2).build();

    var documentTemplateDto3 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(3).build();
    var documentInstanceDto3 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto3).build();

    when(documentInstanceService.getDocumentInstanceDtosByItemReference(itemReference))
        .thenReturn(List.of(documentInstanceDto2, documentInstanceDto1, documentInstanceDto3));

    assertThat(fieldConsentsDocumentInstanceService.getDocumentInstanceSummaryViews(application))
        .containsExactly(
            DocumentInstanceSummaryView.from(documentInstanceDto1),
            DocumentInstanceSummaryView.from(documentInstanceDto2),
            DocumentInstanceSummaryView.from(documentInstanceDto3)
        );
  }

  @Test
  void renderPdf() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var pdfRenderingOptions = PdfRenderingOptions.newBuilder().build();

    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    Map<String, Object> expectedTemplateModel = Map.of(
        "documentInstanceSectionSummaryViews",
        fieldConsentsDocumentInstanceSectionService.getDocumentInstanceSectionSummaryViews(documentInstanceDto),
        "previewWatermark",
        pdfRenderingOptions.previewWatermark()
    );

    when(documentInstanceService.renderPdf(documentInstanceDto, expectedTemplateModel))
        .thenReturn(byteArrayResource);

    assertThat(fieldConsentsDocumentInstanceService.renderPdf(documentInstanceDto, pdfRenderingOptions)).isEqualTo(byteArrayResource);
  }

  @Test
  void getConsentDocumentType_applicationTypeIsProduction() {
    var application = new Application();
    application.setType(ApplicationType.PRODUCTION);

    assertThat(fieldConsentsDocumentInstanceService.getConsentDocumentType(application))
        .isEqualTo(DocumentTemplateType.FIELD_PRODUCTION_CONSENT);
  }

  @Test
  void getConsentDocumentType_applicationTypeIsFlareAndPrimaryAssetIsField() {
    var application = new Application();
    application.setType(ApplicationType.FLARE);

    doReturn(true).when(fieldConsentsDocumentInstanceService).isPrimaryAssetField(application);

    assertThat(fieldConsentsDocumentInstanceService.getConsentDocumentType(application))
        .isEqualTo(DocumentTemplateType.FIELD_FLARE_CONSENT);
  }

  @Test
  void getConsentDocumentType_applicationTypeIsFlareAndPrimaryAssetIsNotField() {
    var application = new Application();
    application.setType(ApplicationType.FLARE);

    doReturn(false).when(fieldConsentsDocumentInstanceService).isPrimaryAssetField(application);

    assertThat(fieldConsentsDocumentInstanceService.getConsentDocumentType(application))
        .isEqualTo(DocumentTemplateType.TERMINAL_FLARE_CONSENT);
  }

  @Test
  void getConsentDocumentType_applicationTypeIsVent() {
    var application = new Application();
    application.setType(ApplicationType.VENT);

    assertThat(fieldConsentsDocumentInstanceService.getConsentDocumentType(application))
        .isEqualTo(DocumentTemplateType.FIELD_VENT_CONSENT);
  }

  @Test
  void isPrimaryAssetField_assetTypeIsField() {
    var application = new Application(1);
    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(AssetType.FIELD);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    assertThat(fieldConsentsDocumentInstanceService.isPrimaryAssetField(application)).isEqualTo(true);
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, names = "FIELD", mode = EnumSource.Mode.EXCLUDE)
  void isPrimaryAssetField_assetTypeIsNotField(AssetType assetType) {
    var application = new Application(1);
    var applicationVersion = new ApplicationVersion();

    var primaryAsset = new ApplicationAsset();
    primaryAsset.setAssetType(assetType);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryAsset);

    assertThat(fieldConsentsDocumentInstanceService.isPrimaryAssetField(application)).isEqualTo(false);
  }

  @Test
  void getItemReference() {
    var application = new Application(1);

    assertThat(fieldConsentsDocumentInstanceService.getItemReference(application))
        .isEqualTo(application.getId().toString());
  }
}
