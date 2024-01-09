package uk.co.nstauthority.fieldconsents.document.lib;

class DocumentTemplateSectionConditionTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String mnemonic = "TEST_MNEMONIC";
    private String title = "Test title";

    private Builder() {
    }

    Builder withMnemonic(String mnemonic) {
      this.mnemonic = mnemonic;
      return this;
    }

    Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    DocumentTemplateSectionCondition build() {
      return new TestDocumentTemplateSectionCondition(mnemonic, title);
    }
  }

  static class TestDocumentTemplateSectionCondition implements DocumentTemplateSectionCondition {

    private final String mnemonic;
    private final String title;

    TestDocumentTemplateSectionCondition(String mnemonic, String title) {
      this.mnemonic = mnemonic;
      this.title = title;
    }

    @Override
    public String getMnemonic() {
      return mnemonic;
    }

    @Override
    public String getTitle() {
      return title;
    }

    @Override
    public boolean evaluate(DocumentInstanceDto documentInstanceDto) {
      return false;
    }
  }
}
