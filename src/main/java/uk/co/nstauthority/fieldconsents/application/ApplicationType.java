package uk.co.nstauthority.fieldconsents.application;

public enum ApplicationType {

  PRODUCTION("Production"),
  FLARE("Flare"),
  VENT("Vent");

  private final String displayName;

  ApplicationType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
