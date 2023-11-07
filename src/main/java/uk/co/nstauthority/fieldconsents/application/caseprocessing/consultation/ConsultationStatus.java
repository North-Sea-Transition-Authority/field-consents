package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

public enum ConsultationStatus {
  OPEN("Open"),
  CLOSED("Closed");

  private final String displayName;

  ConsultationStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
