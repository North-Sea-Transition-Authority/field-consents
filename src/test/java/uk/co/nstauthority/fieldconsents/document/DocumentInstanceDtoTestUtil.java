package uk.co.nstauthority.fieldconsents.document;

import java.util.UUID;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

class DocumentInstanceDtoTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private String itemReference = "TEST_ITEM_REFERENCE";
    private String itemType = "TEST_ITEM_TYPE";
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

    Builder withDocumentTemplate(DocumentTemplateDto documentTemplateDto) {
      this.documentTemplateDto = documentTemplateDto;
      return this;
    }

    DocumentInstanceDto build() {
      return new DocumentInstanceDto(id, itemReference, itemType, documentTemplateDto);
    }
  }
}
