package uk.co.nstauthority.fieldconsents.document.lib;

class DocumentMailMergeFieldTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String mnemonic = "TEST_MNEMONIC";
    private String description = "Test description";

    private Builder() {
    }

    Builder withMnemonic(String mnemonic) {
      this.mnemonic = mnemonic;
      return this;
    }

    Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    DocumentMailMergeField build() {
      return new TestDocumentMailMergeField(mnemonic, description);
    }
  }

  static class TestDocumentMailMergeField implements DocumentMailMergeField {

    private final String mnemonic;
    private final String description;

    TestDocumentMailMergeField(String mnemonic, String description) {
      this.mnemonic = mnemonic;
      this.description = description;
    }

    @Override
    public String getMnemonic() {
      return mnemonic;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
      return false;
    }

    @Override
    public String resolve(DocumentInstanceDto documentInstanceDto) {
      return null;
    }
  }
}
