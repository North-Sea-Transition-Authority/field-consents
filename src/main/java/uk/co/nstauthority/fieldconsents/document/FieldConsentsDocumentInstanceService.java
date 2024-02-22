package uk.co.nstauthority.fieldconsents.document;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;

@Service
public class FieldConsentsDocumentInstanceService {

  private static final String APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE = "APPLICATION";
  private static final Logger LOGGER = LoggerFactory.getLogger(FieldConsentsDocumentInstanceService.class);

  private final DocumentInstanceService documentInstanceService;
  private final DocumentTemplateService documentTemplateService;
  private final FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;
  private final ApplicationVersionService applicationVersionService;
  private final ApplicationAssetService applicationAssetService;

  @Autowired
  FieldConsentsDocumentInstanceService(
      DocumentInstanceService documentInstanceService,
      DocumentTemplateService documentTemplateService,
      FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService,
      ApplicationVersionService applicationVersionService,
      ApplicationAssetService applicationAssetService
  ) {
    this.documentInstanceService = documentInstanceService;
    this.documentTemplateService = documentTemplateService;
    this.fieldConsentsDocumentInstanceSectionService = fieldConsentsDocumentInstanceSectionService;
    this.applicationVersionService = applicationVersionService;
    this.applicationAssetService = applicationAssetService;
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

  public List<DocumentInstanceSummaryView> getDocumentInstanceSummaryViews(Application application) {
    return documentInstanceService.getDocumentInstanceDtosByItemReference(getItemReference(application))
        .stream()
        .sorted(Comparator.comparingInt(documentInstance -> documentInstance.documentTemplateDto().displayOrder()))
        .map(DocumentInstanceSummaryView::from)
        .toList();
  }

  public ByteArrayResource renderPdf(
      DocumentInstanceDto documentInstanceDto,
      PdfRenderingOptions pdfRenderingOptions
  ) {
    Map<String, Object> templateModel = Map.of(
        "documentInstanceSectionSummaryViews",
        fieldConsentsDocumentInstanceSectionService.getDocumentInstanceSectionSummaryViews(documentInstanceDto),
        "previewWatermark",
        pdfRenderingOptions.previewWatermark()
    );

    return documentInstanceService.renderPdf(documentInstanceDto, templateModel);
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
