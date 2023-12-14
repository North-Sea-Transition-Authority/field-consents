package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class DocumentInstanceSectionTemplateCopyingService {

  private final DocumentInstanceSectionRepository documentInstanceSectionRepository;
  private final DocumentTemplateSectionService documentTemplateSectionService;

  @Autowired
  DocumentInstanceSectionTemplateCopyingService(
      DocumentInstanceSectionRepository documentInstanceSectionRepository,
      DocumentTemplateSectionService documentTemplateSectionService
  ) {
    this.documentInstanceSectionRepository = documentInstanceSectionRepository;
    this.documentTemplateSectionService = documentTemplateSectionService;
  }

  void copyDocumentTemplateSectionsToDocumentInstance(
      DocumentTemplate documentTemplate,
      DocumentInstance documentInstance
  ) {
    var documentTemplateSections = documentTemplateSectionService.getDocumentTemplateSections(documentTemplate);

    var documentInstanceSectionsByTemplateSection = new HashMap<DocumentTemplateSection, DocumentInstanceSection>();

    documentTemplateSections.forEach(documentTemplateSection -> {
      var documentInstanceSection = new DocumentInstanceSection();

      documentInstanceSection.setDocumentInstance(documentInstance);
      documentInstanceSection.setCreatedFromDocumentTemplateSection(documentTemplateSection);
      documentInstanceSection.setTitle(documentTemplateSection.getTitle());
      documentInstanceSection.setContent(documentTemplateSection.getContent());
      documentInstanceSection.setDisplayOrder(documentTemplateSection.getDisplayOrder());

      documentInstanceSectionsByTemplateSection.put(documentTemplateSection, documentInstanceSection);
    });

    documentInstanceSectionsByTemplateSection.forEach((documentTemplateSection, documentInstanceSection) -> {
      var documentTemplateSectionParent = documentTemplateSection.getParent();
      if (documentTemplateSectionParent == null) {
        return;
      }

      var documentInstanceSectionParent = documentInstanceSectionsByTemplateSection.get(documentTemplateSectionParent);
      if (documentInstanceSectionParent == null) {
        throw new IllegalStateException(
            "Unable to find document instance section for document template section %s"
                .formatted(documentTemplateSectionParent.getId())
        );
      }

      documentInstanceSection.setParent(documentInstanceSectionParent);
    });

    documentInstanceSectionRepository.saveAll(documentInstanceSectionsByTemplateSection.values());
  }
}
