package uk.co.nstauthority.fieldconsents.document.lib;

class DocumentInstanceSectionFormTestUtil {

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String title = "Test form title";
    private String content = "Test form content";
    private Boolean numbered = true;
    private Boolean hasPageBreakBefore = false;

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

    Builder withNumbered(Boolean numbered) {
      this.numbered = numbered;
      return this;
    }

    Builder withPageBreakBefore(Boolean hasPageBreakBefore) {
      this.hasPageBreakBefore = hasPageBreakBefore;
      return this;
    }

    DocumentInstanceSectionForm build() {
      return new DocumentInstanceSectionForm(title, content, numbered, hasPageBreakBefore);
    }
  }
}
