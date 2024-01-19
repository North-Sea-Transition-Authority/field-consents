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
public class DocumentInstanceSectionService {

  private final DocumentInstanceSectionRepository documentInstanceSectionRepository;
  private final DocumentInstanceService documentInstanceService;

  @Autowired
  DocumentInstanceSectionService(
      DocumentInstanceSectionRepository documentInstanceSectionRepository,
      DocumentInstanceService documentInstanceService
  ) {
    this.documentInstanceSectionRepository = documentInstanceSectionRepository;
    this.documentInstanceService = documentInstanceService;
  }

  @Transactional
  public DocumentInstanceSectionDto createDocumentInstanceSection(
      DocumentInstanceDto documentInstanceDto,
      @Nullable DocumentInstanceSectionDto parentDto,
      String title,
      String content,
      boolean numbered,
      int displayOrder
  ) {
    var documentInstance = documentInstanceService.getDocumentInstanceOrThrow(documentInstanceDto.id());

    var documentInstanceSection = new DocumentInstanceSection();

    documentInstanceSection.setDocumentInstance(documentInstance);
    if (parentDto != null) {
      documentInstanceSection.setParent(getDocumentInstanceSectionOrThrow(parentDto.id()));
    }
    documentInstanceSection.setTitle(title);
    documentInstanceSection.setContent(content);
    documentInstanceSection.setNumbered(numbered);
    documentInstanceSection.setDisplayOrder(displayOrder);

    var documentInstanceSectionsToSave = new ArrayList<DocumentInstanceSection>();

    documentInstanceSectionsToSave.add(documentInstanceSection);

    // If a sibling with the same display order exists, shift its display order up by 1 and any following siblings
    var siblingsWithEqualOrGreaterDisplayOrder =
        documentInstanceSectionRepository.findAllByParent_IdAndDisplayOrderGreaterThanEqual(
                parentDto != null ? parentDto.id() : null,
                displayOrder
            )
            .stream()
            .collect(Collectors.toMap(DocumentInstanceSection::getDisplayOrder, Function.identity()));

    for (int i = displayOrder; siblingsWithEqualOrGreaterDisplayOrder.containsKey(i); i++) {
      var siblingToShift = siblingsWithEqualOrGreaterDisplayOrder.get(i);

      siblingToShift.setDisplayOrder(i + 1);

      documentInstanceSectionsToSave.add(siblingToShift);
    }

    documentInstanceSectionRepository.saveAll(documentInstanceSectionsToSave);

    return DocumentInstanceSectionDto.from(0, documentInstanceSection, List.of());
  }

  @Transactional
  public void editDocumentInstanceSection(
      DocumentInstanceSectionDto documentInstanceSectionDto,
      String title,
      String content,
      boolean numbered
  ) {
    var documentInstanceSection = getDocumentInstanceSectionOrThrow(documentInstanceSectionDto.id());

    documentInstanceSection.setTitle(title);
    documentInstanceSection.setContent(content);
    documentInstanceSection.setNumbered(numbered);

    documentInstanceSectionRepository.save(documentInstanceSection);
  }

  @Transactional
  public void deleteDocumentInstanceSection(DocumentInstanceSectionDto documentInstanceSectionDto) {
    var idsToDelete = new ArrayList<UUID>();

    var documentInstanceSectionId = documentInstanceSectionDto.id();
    idsToDelete.add(documentInstanceSectionId);

    var descendantSectionIds = documentInstanceSectionDto.descendants().stream()
        .map(DocumentInstanceSectionDto::id)
        .toList();
    idsToDelete.addAll(descendantSectionIds);

    documentInstanceSectionRepository.deleteAllById(idsToDelete);
  }

  public DocumentInstanceSectionDto getDocumentInstanceSectionDtoOrThrow(UUID documentInstanceSectionId) {
    var documentInstanceSection = getDocumentInstanceSectionOrThrow(documentInstanceSectionId);
    var allDocumentInstanceSections = documentInstanceSectionRepository.findAllByDocumentInstanceId(
        documentInstanceSection.getDocumentInstance().getId()
    );

    return getDocumentInstanceSectionDto(0, documentInstanceSection, allDocumentInstanceSections);
  }

  DocumentInstanceSectionDto getDocumentInstanceSectionDto(
      int nestingLevel,
      DocumentInstanceSection documentInstanceSection,
      List<DocumentInstanceSection> allDocumentInstanceSections
  ) {
    var childrenDtos = allDocumentInstanceSections.stream()
        .filter(section -> section.getParent() != null
            && section.getParent().getId().equals(documentInstanceSection.getId()))
        .map(child -> getDocumentInstanceSectionDto(nestingLevel + 1, child, allDocumentInstanceSections))
        .toList();

    return DocumentInstanceSectionDto.from(nestingLevel, documentInstanceSection, childrenDtos);
  }

  DocumentInstanceSection getDocumentInstanceSectionOrThrow(UUID documentInstanceSectionId) {
    return documentInstanceSectionRepository.findById(documentInstanceSectionId)
        .orElseThrow(() ->
            new DocumentInstanceSectionNotFoundException(
                "Unable to find document instance section %s".formatted(documentInstanceSectionId)
            )
        );
  }

  public List<DocumentInstanceSectionDto> getTopLevelDocumentInstanceSectionDtos(
      DocumentInstanceDto documentInstanceDto
  ) {
    var allDocumentInstanceSections =
        documentInstanceSectionRepository.findAllByDocumentInstanceId(documentInstanceDto.id());

    return allDocumentInstanceSections.stream()
        .filter(documentInstanceSection -> documentInstanceSection.getParent() == null)
        .map(documentInstanceSection ->
            getDocumentInstanceSectionDto(
                0,
                documentInstanceSection,
                allDocumentInstanceSections
            )
        )
        .toList();
  }
}
