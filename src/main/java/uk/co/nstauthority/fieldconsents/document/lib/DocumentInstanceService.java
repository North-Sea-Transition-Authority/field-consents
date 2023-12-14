package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentInstanceService {

  private final DocumentInstanceRepository documentInstanceRepository;
  private final DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService;
  private final DocumentTemplateService documentTemplateService;

  @Autowired
  DocumentInstanceService(
      DocumentInstanceRepository documentInstanceRepository,
      DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService,
      DocumentTemplateService documentTemplateService
  ) {
    this.documentInstanceRepository = documentInstanceRepository;
    this.documentInstanceSectionTemplateCopyingService = documentInstanceSectionTemplateCopyingService;
    this.documentTemplateService = documentTemplateService;
  }

  @Transactional
  public DocumentInstanceDto createDocumentInstance(
      String itemReference,
      String itemType,
      DocumentTemplateDto documentTemplateDto
  ) {
    var documentTemplate = documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id());

    var documentInstance = new DocumentInstance();

    documentInstance.setItemReference(itemReference);
    documentInstance.setItemType(itemType);
    documentInstance.setDocumentTemplate(documentTemplate);

    documentInstanceRepository.save(documentInstance);

    documentInstanceSectionTemplateCopyingService.copyDocumentTemplateSectionsToDocumentInstance(
        documentTemplate,
        documentInstance
    );

    return DocumentInstanceDto.from(documentInstance);
  }

  public DocumentInstanceDto getDocumentInstanceDtoOrThrow(UUID documentInstanceId) {
    return DocumentInstanceDto.from(getDocumentInstanceOrThrow(documentInstanceId));
  }

  DocumentInstance getDocumentInstanceOrThrow(UUID documentInstanceId) {
    return documentInstanceRepository.findById(documentInstanceId)
        .orElseThrow(() ->
            new DocumentInstanceNotFoundException("Unable to find document instance %s".formatted(documentInstanceId))
        );
  }
}
