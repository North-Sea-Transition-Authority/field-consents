package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public record DocumentTemplateSectionDto(
    UUID id,
    DocumentTemplateDto documentTemplateDto,
    @Nullable UUID parentId,
    String title,
    String content,
    int displayOrder,
    List<DocumentTemplateSectionDto> children
) {

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
        documentTemplateSection.getDisplayOrder(),
        children
    );
  }
}
