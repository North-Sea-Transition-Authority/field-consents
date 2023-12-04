package uk.co.nstauthority.fieldconsents.document.lib;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
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

    documentTemplateSectionRepository.save(documentTemplateSection);

    return DocumentTemplateSectionDto.from(documentTemplateSection, List.of());
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
    var documentTemplateSections =
        documentTemplateSectionRepository.findAllByDocumentTemplateId(documentTemplateDto.id());

    var documentTemplateSectionsByParentId = documentTemplateSections.stream()
        .collect(
            Multimaps.toMultimap(
                section -> section.getParent() == null ? null : section.getParent().getId(),
                Function.identity(),
                ArrayListMultimap::create
            )
        );

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
}
