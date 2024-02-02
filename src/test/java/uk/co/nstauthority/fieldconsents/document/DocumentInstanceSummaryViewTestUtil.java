package uk.co.nstauthority.fieldconsents.document;

import java.util.UUID;

public class DocumentInstanceSummaryViewTestUtil {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private UUID documentInstanceId = UUID.randomUUID();
    private String title = "title";
    private String description = "description";
    private String viewUrl = "/view";

    public Builder withDocumentInstanceId(UUID documentInstanceId) {
      this.documentInstanceId = documentInstanceId;
      return this;
    }

    public Builder withTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder withViewUrl(String viewUrl) {
      this.viewUrl = viewUrl;
      return this;
    }

    public DocumentInstanceSummaryView build() {
      return new DocumentInstanceSummaryView(
          documentInstanceId,
          title,
          description,
          viewUrl
      );
    }

  }

}
