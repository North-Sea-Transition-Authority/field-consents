package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;

class DocumentInstanceDtoTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private String itemReference = "TEST_ITEM_REFERENCE";
    private String itemType = "TEST_ITEM_TYPE";
    private String title = "Test title";
    private String description = "Test description";
    private DocumentTemplateDto documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

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

    Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    Builder withDocumentTemplate(DocumentTemplateDto documentTemplateDto) {
      this.documentTemplateDto = documentTemplateDto;
      return this;
    }

    DocumentInstanceDto build() {
      return new DocumentInstanceDto(id, itemReference, itemType, title, description, documentTemplateDto);
    }
  }
}
