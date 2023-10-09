package uk.co.nstauthority.fieldconsents.assets;

public enum AssetType {

  FIELD("Field"),
  TERMINAL("Facility");

  private final String displayName;

  AssetType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
