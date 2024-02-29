package uk.co.nstauthority.fieldconsents.document;

import java.util.List;
import java.util.UUID;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionDto;

class DocumentTemplateSectionDtoTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private DocumentTemplateDto documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    private UUID parentId;
    private String title = "Test title";
    private String content = "Test content";
    private String conditionMnemonic;
    private boolean numbered = true;
    private boolean hasPageBreakBefore = false;
    private int displayOrder = 1;
    private List<DocumentTemplateSectionDto> children = List.of();

    private Builder() {
    }

    Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    Builder withDocumentTemplateDto(DocumentTemplateDto documentTemplateDto) {
      this.documentTemplateDto = documentTemplateDto;
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

    Builder withConditionMnemonic(String conditionMnemonic) {
      this.conditionMnemonic = conditionMnemonic;
      return this;
    }

    Builder withNumbered(boolean numbered) {
      this.numbered = numbered;
      return this;
    }

    Builder withPageBreakBefore(boolean hasPageBreakBefore) {
      this.hasPageBreakBefore = hasPageBreakBefore;
      return this;
    }

    Builder withDisplayOrder(int displayOrder) {
      this.displayOrder = displayOrder;
      return this;
    }

    Builder withChildren(List<DocumentTemplateSectionDto> children) {
      this.children = children;
      return this;
    }

    DocumentTemplateSectionDto build() {
      return new DocumentTemplateSectionDto(
          id,
          documentTemplateDto,
          parentId,
          title,
          content,
          conditionMnemonic,
          numbered,
          hasPageBreakBefore,
          displayOrder,
          children
      );
    }
  }
}
