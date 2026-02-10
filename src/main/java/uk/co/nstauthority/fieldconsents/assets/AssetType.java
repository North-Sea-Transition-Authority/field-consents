package uk.co.nstauthority.fieldconsents.assets;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum AssetType implements Displayable {

  FIELD("Field"),
  TERMINAL("Facility"),
  FACILITY("Facility"),
  HUB("Hub");

  private final String displayName;

  AssetType(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }
}
