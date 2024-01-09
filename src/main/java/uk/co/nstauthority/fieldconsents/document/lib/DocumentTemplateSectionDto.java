package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public record DocumentTemplateSectionDto(
    UUID id,
    DocumentTemplateDto documentTemplateDto,
    @Nullable UUID parentId,
    String title,
    String content,
    String conditionMnemonic,
    int displayOrder,
    List<DocumentTemplateSectionDto> children
) {

  public List<DocumentTemplateSectionDto> descendants() {
    return children().stream()
        .flatMap(child -> Stream.concat(Stream.of(child), child.descendants().stream()))
        .toList();
  }

  static DocumentTemplateSectionDto from(
      DocumentTemplateSection documentTemplateSection,
      List<DocumentTemplateSectionDto> children
  ) {
    var parent = documentTemplateSection.getParent();

    return new DocumentTemplateSectionDto(
        documentTemplateSection.getId(),
        DocumentTemplateDto.from(documentTemplateSection.getDocumentTemplate()),
        parent != null ? parent.getId() : null,
        documentTemplateSection.getTitle(),
        documentTemplateSection.getContent(),
        documentTemplateSection.getConditionMnemonic(),
        documentTemplateSection.getDisplayOrder(),
        children
    );
  }
}
