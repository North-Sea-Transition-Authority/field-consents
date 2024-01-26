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

  @Autowired
  FieldConsentsDocumentInstanceService(
      DocumentInstanceService documentInstanceService,
      DocumentTemplateService documentTemplateService,
      FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService
  ) {
    this.documentInstanceService = documentInstanceService;
    this.documentTemplateService = documentTemplateService;
    this.fieldConsentsDocumentInstanceSectionService = fieldConsentsDocumentInstanceSectionService;
  }

  public void createDocumentInstancesForApplication(Application application) {
    var applicationId = application.getId();
    var consentDocumentType = getConsentDocumentType(application);
    var itemReference = getItemReference(application);

    var documentTemplateDto = documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(consentDocumentType.getMnemonic());
    var documentInstanceDtoOptional = documentInstanceService
        .getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(
            itemReference,
            APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
            documentTemplateDto
        );

    if (documentInstanceDtoOptional.isPresent()) {
      LOGGER.debug(
          "Not creating consent document instance for application [{}], since one or more already exist",
          applicationId
      );
      return;
    }

    createDocumentInstance(application, documentTemplateDto, consentDocumentType);
    LOGGER.debug("Created consent document instance for application [{}]", applicationId);
  }

  public DocumentInstanceDto createDocumentInstance(
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

  private DocumentTemplateType getConsentDocumentType(Application application) {
    return switch (application.getType()) {
      case PRODUCTION -> DocumentTemplateType.FIELD_PRODUCTION_CONSENT;
      case FLARE -> DocumentTemplateType.FIELD_FLARE_CONSENT;
      case VENT -> DocumentTemplateType.FIELD_VENT_CONSENT;
    };
  }

  private String getItemReference(Application application) {
    return application.getId().toString();
  }
}
