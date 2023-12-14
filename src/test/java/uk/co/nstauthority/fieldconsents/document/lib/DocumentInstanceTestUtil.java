package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;

class DocumentInstanceTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private String itemReference = "TEST_ITEM_REFERENCE";
    private String itemType = "TEST_ITEM_TYPE";
    private DocumentTemplate documentTemplate = DocumentTemplateTestUtil.builder().build();

    private Builder() {
    }

    Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    Builder withItemReference(String itemReference) {
      this.itemReference = itemReference;
      return this;
    }

    Builder withItemType(String itemType) {
      this.itemType = itemType;
      return this;
    }

    Builder withDocumentTemplate(DocumentTemplate documentTemplate) {
      this.documentTemplate = documentTemplate;
      return this;
    }

    DocumentInstance build() {
      var documentInstance = new DocumentInstance(id);

      documentInstance.setItemReference(itemReference);
      documentInstance.setItemType(itemType);
      documentInstance.setDocumentTemplate(documentTemplate);

      return documentInstance;
    }
  }
}
