package uk.co.nstauthority.fieldconsents.flarevent;

public enum FlareVentUnit {
  TONNES_PER_MONTH("tonnes/month");

  private final String displayName;

  FlareVentUnit(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
