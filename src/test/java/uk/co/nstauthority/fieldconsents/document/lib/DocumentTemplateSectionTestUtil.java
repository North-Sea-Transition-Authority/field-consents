package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;

class DocumentTemplateSectionTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private DocumentTemplate documentTemplate = DocumentTemplateTestUtil.builder().build();
    private DocumentTemplateSection parent;
    private String title = "Test title";
    private String content = "Test content";
    private int displayOrder = 1;

    private Builder() {
    }

    Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    Builder withDocumentTemplate(DocumentTemplate documentTemplate) {
      this.documentTemplate = documentTemplate;
      return this;
    }

    Builder withParent(DocumentTemplateSection parent) {
      this.parent = parent;
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

    Builder withDisplayOrder(int displayOrder) {
      this.displayOrder = displayOrder;
      return this;
    }

    DocumentTemplateSection build() {
      var documentTemplateSection = new DocumentTemplateSection(id);

      documentTemplateSection.setDocumentTemplate(documentTemplate);
      documentTemplateSection.setParent(parent);
      documentTemplateSection.setTitle(title);
      documentTemplateSection.setContent(content);
      documentTemplateSection.setDisplayOrder(displayOrder);

      return documentTemplateSection;
    }
  }
}
