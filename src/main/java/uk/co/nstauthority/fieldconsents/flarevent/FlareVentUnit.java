package uk.co.nstauthority.fieldconsents.flarevent;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum FlareVentUnit implements Displayable {
  TONNES_PER_MONTH("tonnes/month"),
  TONNES_PER_DAY("tonnes/day"),
  KG_PER_CUBIC_METER("kg/m3"),
  MASS_PERCENTAGE("mass %"),
  G_PER_MOL("g/mol"),
  MOL_PERCENTAGE("mol %");

  private final String displayName;

  FlareVentUnit(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }
}
