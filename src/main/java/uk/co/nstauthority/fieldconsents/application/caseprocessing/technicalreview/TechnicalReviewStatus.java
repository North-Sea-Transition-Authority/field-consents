package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

public enum TechnicalReviewStatus {
  OPEN("Open"),
  CLOSED("Closed");

  TechnicalReviewStatus(String displayName) {
    this.displayName = displayName;
  }

  private final String displayName;

  public String getDisplayName() {
    return displayName;
  }
}
