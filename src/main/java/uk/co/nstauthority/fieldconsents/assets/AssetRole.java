package uk.co.nstauthority.fieldconsents.assets;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum AssetRole implements Displayable {
  PRIMARY("Primary", 10),
  SECONDARY("Additional", 20);

  private final String displayName;

  private final int displayOrder;

  AssetRole(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  @Override
  public String getEnumName() {
    return this.name();
  }
}
