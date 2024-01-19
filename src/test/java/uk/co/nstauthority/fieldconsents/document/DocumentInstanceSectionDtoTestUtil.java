package uk.co.nstauthority.fieldconsents.document;

import java.util.List;
import java.util.UUID;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceSectionDto;

class DocumentInstanceSectionDtoTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private DocumentInstanceDto documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    private UUID createdFromDocumentTemplateSectionId = UUID.randomUUID();
    private UUID parentId;
    private String title = "Test title";
    private String content = "Test content";
    private boolean numbered = true;
    private int displayOrder = 1;
    private int nestingLevel = 0;
    private List<DocumentInstanceSectionDto> children = List.of();

    private Builder() {
    }

    Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    Builder withDocumentInstanceDto(DocumentInstanceDto documentInstanceDto) {
      this.documentInstanceDto = documentInstanceDto;
      return this;
    }

    Builder withCreatedFromDocumentTemplateSectionId(UUID createdFromDocumentTemplateSectionId) {
      this.createdFromDocumentTemplateSectionId = createdFromDocumentTemplateSectionId;
      return this;
    }

    Builder withParentId(UUID parentId) {
      this.parentId = parentId;
      return this;
    }

    Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    Builder withContent(String content) {
      this.content = content;
      return this;
    }

    Builder withNumbered(boolean numbered) {
      this.numbered = numbered;
      return this;
    }

    Builder withDisplayOrder(int displayOrder) {
      this.displayOrder = displayOrder;
      return this;
    }

    Builder withNestingLevel(int nestingLevel) {
      this.nestingLevel = nestingLevel;
      return this;
    }

    Builder withChildren(List<DocumentInstanceSectionDto> children) {
      this.children = children;
      return this;
    }

    DocumentInstanceSectionDto build() {
      return new DocumentInstanceSectionDto(
          id,
          documentInstanceDto,
          createdFromDocumentTemplateSectionId,
          parentId,
          title,
          content,
          numbered,
          displayOrder,
          nestingLevel,
          children
      );
    }
  }
}
