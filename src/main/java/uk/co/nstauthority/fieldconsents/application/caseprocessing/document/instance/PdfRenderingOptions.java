package uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance;

public record PdfRenderingOptions(
    boolean previewWatermark
) {

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private boolean previewWatermark;

    public Builder withPreviewWatermark(boolean previewWatermark) {
      this.previewWatermark = previewWatermark;
      return this;
    }

    public PdfRenderingOptions build() {
      return new PdfRenderingOptions(previewWatermark);
    }

  }

}
