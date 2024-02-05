package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class DocumentInstanceSectionTemplateCopyingService {

  private final DocumentInstanceSectionRepository documentInstanceSectionRepository;
  private final DocumentTemplateSectionService documentTemplateSectionService;
  private final DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Autowired
  DocumentInstanceSectionTemplateCopyingService(
      DocumentInstanceSectionRepository documentInstanceSectionRepository,
      DocumentTemplateSectionService documentTemplateSectionService,
      DocumentTemplateSectionConditionService documentTemplateSectionConditionService
  ) {
    this.documentInstanceSectionRepository = documentInstanceSectionRepository;
    this.documentTemplateSectionService = documentTemplateSectionService;
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
  }

  void copyDocumentTemplateSectionsToDocumentInstance(DocumentInstance documentInstance) {
    var allDocumentTemplateSections =
        documentTemplateSectionService.getDocumentTemplateSections(documentInstance.getDocumentTemplate());
    var copiedDocumentInstanceSections = allDocumentTemplateSections.stream()
        .filter(section -> section.getParent() == null)
        .flatMap(child ->
            tryCopyDocumentTemplateSectionAndChildren(
                child,
                documentInstance,
                null,
                allDocumentTemplateSections
            ).stream()
        )
        .toList();

    documentInstanceSectionRepository.saveAll(copiedDocumentInstanceSections);
  }

  List<DocumentInstanceSection> tryCopyDocumentTemplateSectionAndChildren(
      DocumentTemplateSection documentTemplateSection,
      DocumentInstance documentInstance,
      DocumentInstanceSection parent,
      List<DocumentTemplateSection> allDocumentTemplateSections
  ) {
    var conditionMnemonic = documentTemplateSection.getConditionMnemonic();
    if (conditionMnemonic != null) {
      var condition = documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionConditionOrThrow(
          DocumentTemplateDto.from(documentInstance.getDocumentTemplate()),
          conditionMnemonic
      );
      if (!condition.evaluate(DocumentInstanceDto.from(documentInstance))) {
        return List.of();
      }
    }

    var documentInstanceSection = newDocumentInstanceSection(documentTemplateSection, documentInstance, parent);

    var copiedChildren = allDocumentTemplateSections.stream()
        .filter(section -> section.getParent() != null
            && section.getParent().getId().equals(documentTemplateSection.getId()))
        .flatMap(child ->
            tryCopyDocumentTemplateSectionAndChildren(
                child,
                documentInstance,
                documentInstanceSection,
                allDocumentTemplateSections
            ).stream()
        );

    return Stream.concat(Stream.of(documentInstanceSection), copiedChildren).toList();
  }

  DocumentInstanceSection newDocumentInstanceSection(
      DocumentTemplateSection documentTemplateSection,
      DocumentInstance documentInstance,
      DocumentInstanceSection parent
  ) {
    var documentInstanceSection = new DocumentInstanceSection();

    documentInstanceSection.setDocumentInstance(documentInstance);
    documentInstanceSection.setCreatedFromDocumentTemplateSection(documentTemplateSection);
    documentInstanceSection.setParent(parent);
    documentInstanceSection.setTitle(documentTemplateSection.getTitle());
    documentInstanceSection.setContent(documentTemplateSection.getContent());
    documentInstanceSection.setNumbered(documentTemplateSection.isNumbered());
    documentInstanceSection.setDisplayOrder(documentTemplateSection.getDisplayOrder());

    return documentInstanceSection;
  }

  void reloadDocumentInstanceSectionsFromDocumentTemplate(DocumentInstance documentInstance) {
    documentInstanceSectionRepository.deleteAllByDocumentInstanceId(documentInstance.getId());

    copyDocumentTemplateSectionsToDocumentInstance(documentInstance);
  }
}
