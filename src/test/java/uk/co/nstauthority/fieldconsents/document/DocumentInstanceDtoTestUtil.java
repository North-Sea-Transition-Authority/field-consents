package uk.co.nstauthority.fieldconsents.document;

import java.util.UUID;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

public class DocumentInstanceDtoTestUtil {

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private UUID id = UUID.randomUUID();
    private String itemReference = "TEST_ITEM_REFERENCE";
    private String itemType = "TEST_ITEM_TYPE";
    private DocumentTemplateDto documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    private Builder() {
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withItemReference(String itemReference) {
      this.itemReference = itemReference;
      return this;
    }

    public Builder withItemType(String itemType) {
      this.itemType = itemType;
      return this;
    }

    public Builder withDocumentTemplate(DocumentTemplateDto documentTemplateDto) {
      this.documentTemplateDto = documentTemplateDto;
      return this;
    }

    public DocumentInstanceDto build() {
      return new DocumentInstanceDto(id, itemReference, itemType, documentTemplateDto);
    }
  }
}
