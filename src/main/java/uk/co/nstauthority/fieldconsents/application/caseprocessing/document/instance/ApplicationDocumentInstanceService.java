package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@Service
public class ApplicationDocumentInstanceService {

  private static final String APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE = "APPLICATION";
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDocumentInstanceService.class);

  private final ApplicationDocumentInstanceSectionViewService applicationDocumentInstanceSectionViewService;
  private final DocumentTemplateService documentTemplateService;
  private final DocumentInstanceService documentInstanceService;
  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationAssetService applicationAssetService;
  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  ApplicationDocumentInstanceService(
      ApplicationDocumentInstanceSectionViewService applicationDocumentInstanceSectionViewService,
      DocumentTemplateService documentTemplateService, DocumentInstanceService documentInstanceService,
      ApplicationService applicationService, ApplicationVersionService applicationVersionService,
      ApplicationAssetService applicationAssetService,
      CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties
  ) {
    this.applicationDocumentInstanceSectionViewService = applicationDocumentInstanceSectionViewService;
    this.documentTemplateService = documentTemplateService;
    this.documentInstanceService = documentInstanceService;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.applicationAssetService = applicationAssetService;
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
  }

  public void createDocumentInstancesForApplication(Application application) {
    var applicationId = application.getId();

    var itemReference = getItemReference(application);

    getApplicableDocumentTemplateTypes(application).forEach(documentTemplateType -> {
      var documentTemplateTypeMnemonic = documentTemplateType.getMnemonic();

      var documentTemplateDto =
          documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(documentTemplateTypeMnemonic);
      var documentInstanceDtoOptional = documentInstanceService
          .getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
              itemReference,
              APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
              documentTemplateDto
          );

      if (documentInstanceDtoOptional.isPresent()) {
        LOGGER.debug(
            "Not creating {} document instance for application [{}], since one or more already exist",
            documentTemplateTypeMnemonic,
            applicationId
        );
        return;
      }

      createDocumentInstance(application, documentTemplateDto, documentTemplateType);
      LOGGER.debug("Created {} document instance for application [{}]", documentTemplateTypeMnemonic, applicationId);
    });
  }

  DocumentInstanceDto createDocumentInstance(
      Application application,
      DocumentTemplateDto documentTemplateDto,
      DocumentTemplateType documentTemplateType
  ) {
    return documentInstanceService.createDocumentInstance(
        getItemReference(application),
        APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
        documentTemplateDto.title(),
        documentTemplateType.getDocumentInstanceDescription(),
        documentTemplateDto
    );
  }

  public List<DocumentInstanceDto> getDocumentInstanceDtos(Application application) {
    return documentInstanceService.getDocumentInstanceDtosByItemReference(getItemReference(application));
  }

  public ByteArrayResource renderPdf(
      ApplicationVersion applicationVersion,
      DocumentInstanceDto documentInstanceDto,
      PdfRenderingOptions pdfRenderingOptions
  ) {
    var documentInstanceSectionsSummaryView =
        applicationDocumentInstanceSectionViewService.getDocumentInstanceSectionsSummaryView(
            applicationVersion.getApplication(),
            documentInstanceDto,
            false
        );

    Map<String, Object> templateModel = Map.of(
        "documentInstanceSectionsSummaryView", documentInstanceSectionsSummaryView,
        "previewWatermark", pdfRenderingOptions.previewWatermark(),
        "applicationReference", applicationService.generateApplicationReference(applicationVersion),
        "customerBrandingConfigurationProperties", customerBrandingConfigurationProperties
    );

    return documentInstanceService.renderPdf(documentInstanceDto, templateModel);
  }

  public boolean mailMergeErrorPresent(Application application) {
    return getDocumentInstanceDtos(application)
        .stream()
        .map(documentInstanceDto -> applicationDocumentInstanceSectionViewService
            .getDocumentInstanceSectionsSummaryView(
                application,
                documentInstanceDto,
                false
            )
        )
        .anyMatch(view -> view.errorMessages() != null && !view.errorMessages().isEmpty());
  }

  List<DocumentTemplateType> getApplicableDocumentTemplateTypes(Application application) {
    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId());
    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);

    return switch (application.getType()) {
      case PRODUCTION -> {
        if (!primaryAsset.isField()) {
          throw new IllegalStateException(
              "Primary asset %d is not field [asset type: %s]".formatted(primaryAsset.getId(), primaryAsset.getAssetType())
          );
        }

        yield isFlareCommissioningLetterApplicableForProductionApplication(primaryAsset)
            ? List.of(DocumentTemplateType.FIELD_PRODUCTION_CONSENT, DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER)
            : List.of(DocumentTemplateType.FIELD_PRODUCTION_CONSENT);
      }
      case FLARE -> primaryAsset.isField()
          ? List.of(DocumentTemplateType.FIELD_FLARE_CONSENT)
          : List.of(DocumentTemplateType.TERMINAL_FLARE_CONSENT);
      case VENT -> primaryAsset.isField()
          ? List.of(DocumentTemplateType.FIELD_VENT_CONSENT)
          : List.of(DocumentTemplateType.TERMINAL_VENT_CONSENT);
    };
  }

  boolean isFlareCommissioningLetterApplicableForProductionApplication(ApplicationAsset primaryAsset) {
    if (!primaryAsset.isField()) {
      return false;
    }

    return !applicationAssetService.completedProductionApplicationExistsWithPrimaryField(primaryAsset.getAssetId());
  }

  String getItemReference(Application application) {
    return application.getId().toString();
  }
}
