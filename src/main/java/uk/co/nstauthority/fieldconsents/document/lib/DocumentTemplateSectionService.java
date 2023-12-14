package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentTemplateSectionService {

  private final DocumentTemplateSectionRepository documentTemplateSectionRepository;
  private final DocumentTemplateService documentTemplateService;

  @Autowired
  DocumentTemplateSectionService(
      DocumentTemplateSectionRepository documentTemplateSectionRepository,
      DocumentTemplateService documentTemplateService
  ) {
    this.documentTemplateSectionRepository = documentTemplateSectionRepository;
    this.documentTemplateService = documentTemplateService;
  }

  @Transactional
  public DocumentTemplateSectionDto createDocumentTemplateSection(
      DocumentTemplateDto documentTemplateDto,
      @Nullable DocumentTemplateSectionDto parentDto,
      String title,
      String content,
      int displayOrder
  ) {
    var documentTemplate = documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id());

    var documentTemplateSection = new DocumentTemplateSection();

    documentTemplateSection.setDocumentTemplate(documentTemplate);
    if (parentDto != null) {
      documentTemplateSection.setParent(getDocumentTemplateSectionOrThrow(parentDto.id()));
    }
    documentTemplateSection.setTitle(title);
    documentTemplateSection.setContent(content);
    documentTemplateSection.setDisplayOrder(displayOrder);

    var documentTemplateSectionsToSave = new ArrayList<DocumentTemplateSection>();

    documentTemplateSectionsToSave.add(documentTemplateSection);

    // If a sibling with the same display order exists, shift its display order up by 1 and any following siblings
    var siblingsWithEqualOrGreaterDisplayOrder =
        documentTemplateSectionRepository.findAllByParent_IdAndDisplayOrderGreaterThanEqual(
            parentDto != null ? parentDto.id() : null,
            displayOrder
        )
            .stream()
            .collect(Collectors.toMap(DocumentTemplateSection::getDisplayOrder, Function.identity()));

    for (int i = displayOrder; siblingsWithEqualOrGreaterDisplayOrder.containsKey(i); i++) {
      var siblingToShift = siblingsWithEqualOrGreaterDisplayOrder.get(i);

      siblingToShift.setDisplayOrder(i + 1);

      documentTemplateSectionsToSave.add(siblingToShift);
    }

    documentTemplateSectionRepository.saveAll(documentTemplateSectionsToSave);

    return DocumentTemplateSectionDto.from(documentTemplateSection, List.of());
  }

  @Transactional
  public void editDocumentTemplateSection(
      DocumentTemplateSectionDto documentTemplateSectionDto,
      String title,
      String content
  ) {
    var documentTemplateSection = getDocumentTemplateSectionOrThrow(documentTemplateSectionDto.id());

    documentTemplateSection.setTitle(title);
    documentTemplateSection.setContent(content);

    documentTemplateSectionRepository.save(documentTemplateSection);
  }

  @Transactional
  public void deleteDocumentTemplateSection(DocumentTemplateSectionDto documentTemplateSectionDto) {
    var idsToDelete = new ArrayList<UUID>();

    var documentTemplateSectionId = documentTemplateSectionDto.id();
    idsToDelete.add(documentTemplateSectionId);

    var descendantSectionIds = documentTemplateSectionDto.descendants().stream()
        .map(DocumentTemplateSectionDto::id)
        .toList();
    idsToDelete.addAll(descendantSectionIds);

    documentTemplateSectionRepository.deleteAllById(idsToDelete);
  }

  public DocumentTemplateSectionDto getDocumentTemplateSectionDtoOrThrow(UUID documentTemplateSectionId) {
    var documentTemplateSection = getDocumentTemplateSectionOrThrow(documentTemplateSectionId);
    var allDocumentTemplateSections = documentTemplateSectionRepository.findAllByDocumentTemplateId(
        documentTemplateSection.getDocumentTemplate().getId()
    );

    return getDocumentTemplateSectionDto(documentTemplateSection, allDocumentTemplateSections);
  }

  DocumentTemplateSectionDto getDocumentTemplateSectionDto(
      DocumentTemplateSection documentTemplateSection,
      List<DocumentTemplateSection> allDocumentTemplateSections
  ) {
    var childrenDtos = allDocumentTemplateSections.stream()
        .filter(section -> section.getParent() != null
            && section.getParent().getId().equals(documentTemplateSection.getId()))
        .map(child -> getDocumentTemplateSectionDto(child, allDocumentTemplateSections))
        .toList();

    return DocumentTemplateSectionDto.from(documentTemplateSection, childrenDtos);
  }

  DocumentTemplateSection getDocumentTemplateSectionOrThrow(UUID documentTemplateSectionId) {
    return documentTemplateSectionRepository.findById(documentTemplateSectionId)
        .orElseThrow(() ->
            new DocumentTemplateSectionNotFoundException(
                "Unable to find document template section %s".formatted(documentTemplateSectionId)
            )
        );
  }

  public List<DocumentTemplateSectionDto> getTopLevelDocumentTemplateSectionDtos(
      DocumentTemplateDto documentTemplateDto
  ) {
    var allDocumentTemplateSections =
        documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplateDto.id());

    return allDocumentTemplateSections.stream()
        .filter(documentTemplateSection -> documentTemplateSection.getParent() == null)
        .map(documentTemplateSection ->
            getDocumentTemplateSectionDto(
                documentTemplateSection,
                allDocumentTemplateSections
            )
        )
        .toList();
  }

  List<DocumentTemplateSection> getDocumentTemplateSections(DocumentTemplate documentTemplate) {
    return documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplate.getId());
  }
}
