package uk.co.nstauthority.fieldconsents.document;

class DocumentSectionFormTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String title = "Test form title";
    private String content = "Test form content";

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

    DocumentSectionForm build() {
      return new DocumentSectionForm(title, content);
    }
  }
}
