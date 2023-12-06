package uk.co.nstauthority.fieldconsents.document.lib;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
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
    var documentTemplateSectionsByParentId =
        getDocumentTemplateSectionsByParentIdMultimap(documentTemplateSection.getDocumentTemplate().getId());

    return getDocumentTemplateSectionDto(documentTemplateSection, documentTemplateSectionsByParentId);
  }

  DocumentTemplateSectionDto getDocumentTemplateSectionDto(
      DocumentTemplateSection documentTemplateSection,
      ListMultimap<UUID, DocumentTemplateSection> documentTemplateSectionsByParentId
  ) {
    var children = documentTemplateSectionsByParentId.get(documentTemplateSection.getId());
    var childrenDtos = children.stream()
        .map(child -> getDocumentTemplateSectionDto(child, documentTemplateSectionsByParentId))
        .toList();

    return DocumentTemplateSectionDto.from(documentTemplateSection, childrenDtos);
  }

  DocumentTemplateSection getDocumentTemplateSectionOrThrow(UUID documentTemplateSectionId) {
    return documentTemplateSectionRepository.findById(documentTemplateSectionId)
        .orElseThrow(() ->
            new DocumentTemplateNotFoundException(
                "Unable to find document template section %s".formatted(documentTemplateSectionId)
            )
        );
  }

  public List<DocumentTemplateSectionDto> getDocumentTemplateSectionDtos(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateSectionsByParentId = getDocumentTemplateSectionsByParentIdMultimap(documentTemplateDto.id());

    var topLevelDocumentTemplateSections = documentTemplateSectionsByParentId.get(null);

    return topLevelDocumentTemplateSections.stream()
        .map(documentTemplateSection ->
            getDocumentTemplateSectionDto(
                documentTemplateSection,
                documentTemplateSectionsByParentId
            )
        )
        .toList();
  }

  ListMultimap<UUID, DocumentTemplateSection> getDocumentTemplateSectionsByParentIdMultimap(
      UUID documentTemplateId
  ) {
    return documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplateId).stream()
        .collect(
            Multimaps.toMultimap(
                section -> section.getParent() == null ? null : section.getParent().getId(),
                Function.identity(),
                ArrayListMultimap::create
            )
        );
  }
}
