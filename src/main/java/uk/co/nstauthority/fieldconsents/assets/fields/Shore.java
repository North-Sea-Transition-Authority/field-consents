package uk.co.nstauthority.fieldconsents.assets.fields;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum Shore implements Displayable {
  OFFSHORE("Offshore", 1),
  ONSHORE("Onshore", 2),
  UNKNOWN("Unknown", 3);

  private final String displayName;

  private final int displayOrder;

  Shore(String displayName, int displayOrder) {
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
}
