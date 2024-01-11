package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;

class DocumentInstanceSectionTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private DocumentInstance documentInstance = DocumentInstanceTestUtil.builder().build();
    private DocumentTemplateSection createdFromDocumentTemplateSection =
        DocumentTemplateSectionTestUtil.builder().build();
    private DocumentInstanceSection parent;
    private String title = "Test title";
    private String content = "Test content";
    private boolean numbered = true;
    private int displayOrder = 1;

    private Builder() {
    }

    Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    Builder withDocumentInstance(DocumentInstance documentInstance) {
      this.documentInstance = documentInstance;
      return this;
    }

    Builder withCreatedFromDocumentTemplateSection(DocumentTemplateSection createdFromDocumentTemplateSection) {
      this.createdFromDocumentTemplateSection = createdFromDocumentTemplateSection;
      return this;
    }

    Builder withParent(DocumentInstanceSection parent) {
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

    Builder withNumbered(boolean numbered) {
      this.numbered = numbered;
      return this;
    }

    Builder withDisplayOrder(int displayOrder) {
      this.displayOrder = displayOrder;
      return this;
    }

    DocumentInstanceSection build() {
      var documentInstanceSection = new DocumentInstanceSection(id);

      documentInstanceSection.setDocumentInstance(documentInstance);
      documentInstanceSection.setCreatedFromDocumentTemplateSection(createdFromDocumentTemplateSection);
      documentInstanceSection.setParent(parent);
      documentInstanceSection.setTitle(title);
      documentInstanceSection.setContent(content);
      documentInstanceSection.setNumbered(numbered);
      documentInstanceSection.setDisplayOrder(displayOrder);

      return documentInstanceSection;
    }
  }
}
