package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;

class DocumentTemplateDtoTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private UUID id = UUID.randomUUID();
    private String mnemonic = "TEST_MNEMONIC";
    private String title = "Test title";
    private String description = "Test description";
    private String templatePath = "test/template/path";
    private int displayOrder = 1;

    private Builder() {
    }

    Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    Builder withMnemonic(String mnemonic) {
      this.mnemonic = mnemonic;
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

    Builder withTemplatePath(String templatePath) {
      this.templatePath = templatePath;
      return this;
    }

    Builder withDisplayOrder(int displayOrder) {
      this.displayOrder = displayOrder;
      return this;
    }

    DocumentTemplateDto build() {
      return new DocumentTemplateDto(id, mnemonic, title, description, templatePath, displayOrder);
    }
  }
}
