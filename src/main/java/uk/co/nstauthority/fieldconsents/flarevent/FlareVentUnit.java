package uk.co.nstauthority.fieldconsents.flarevent;

public enum FlareVentUnit {
  TONNES_PER_MONTH("tonnes/month"),
  KG_PER_CUBIC_METER("kg/m3"),
  MASS_PERCENTAGE("mass %");

  private final String displayName;

  FlareVentUnit(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
