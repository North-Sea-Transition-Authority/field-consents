package uk.co.nstauthority.fieldconsents.application.rationale;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ApplicationRationaleType implements Displayable {

  INCREASE("Increase", 10),
  DECREASE("Decrease", 20),
  NO_CHANGE("No change", 30),
  EXTENSION("Extension", 40),
  OTHER("Other", 50)
  ;

  private final String displayName;
  private final int displayOrder;

  ApplicationRationaleType(String displayName, int displayOrder) {
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
