package uk.co.nstauthority.fieldconsents.document;

class DocumentTemplateSectionFormTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String title = "Test form title";
    private String content = "Test form content";
    private String conditionMnemonic;

    private Builder() {
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

    DocumentTemplateSectionForm build() {
      return new DocumentTemplateSectionForm(title, content, conditionMnemonic);
    }
  }
}
