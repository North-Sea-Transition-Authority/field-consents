package uk.co.nstauthority.fieldconsents.document;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

@Service
public class FieldConsentsDocumentInstanceService {

  private final DocumentInstanceService documentInstanceService;
  private final FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;

  @Autowired
  FieldConsentsDocumentInstanceService(
      DocumentInstanceService documentInstanceService,
      FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService
  ) {
    this.documentInstanceService = documentInstanceService;
    this.fieldConsentsDocumentInstanceSectionService = fieldConsentsDocumentInstanceSectionService;
  }

  public DocumentInstanceDto createDocumentInstance(
      ApplicationVersion applicationVersion,
      DocumentTemplateDto documentTemplateDto
  ) {
    var itemReference = applicationVersion.getId().toString();
    var itemType = documentTemplateDto.mnemonic();

    return documentInstanceService.createDocumentInstance(itemReference, itemType, documentTemplateDto);
  }

  public ByteArrayResource renderPdf(DocumentInstanceDto documentInstanceDto) {
    Map<String, Object> templateModel = Map.of(
        "documentInstanceSectionSummaryViews",
        fieldConsentsDocumentInstanceSectionService.getDocumentInstanceSectionSummaryViews(documentInstanceDto)
    );

    return documentInstanceService.renderPdf(documentInstanceDto, templateModel);
  }
}
