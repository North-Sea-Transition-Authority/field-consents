package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

public enum ApplicationUpdateStatus {
  OPEN("Open"),
  CLOSED("Closed");

  private final String displayName;

  ApplicationUpdateStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
