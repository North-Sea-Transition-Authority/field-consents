package uk.co.nstauthority.fieldconsents.document.lib;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public record DocumentInstanceSectionDto(
    UUID id,
    DocumentInstanceDto documentInstanceDto,
    @Nullable UUID createdFromDocumentTemplateSectionId,
    @Nullable UUID parentId,
    String title,
    String content,
    int displayOrder,
    List<DocumentInstanceSectionDto> children
) implements DocumentSectionDto<DocumentInstanceSectionDto> {

  static DocumentInstanceSectionDto from(
      DocumentInstanceSection documentInstanceSection,
      List<DocumentInstanceSectionDto> children
  ) {
    var createdFromDocumentTemplateSection = documentInstanceSection.getCreatedFromDocumentTemplateSection();
    var parent = documentInstanceSection.getParent();

    return new DocumentInstanceSectionDto(
        documentInstanceSection.getId(),
        DocumentInstanceDto.from(documentInstanceSection.getDocumentInstance()),
        createdFromDocumentTemplateSection != null ? createdFromDocumentTemplateSection.getId() : null,
        parent != null ? parent.getId() : null,
        documentInstanceSection.getTitle(),
        documentInstanceSection.getContent(),
        documentInstanceSection.getDisplayOrder(),
        children
    );
  }
}
