package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ApplicationUpdateStatus implements Displayable {
  OPEN("Open"),
  CLOSED("Closed");

  private final String displayName;

  ApplicationUpdateStatus(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }
}
