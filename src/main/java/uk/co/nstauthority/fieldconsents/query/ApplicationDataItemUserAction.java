package uk.co.nstauthority.fieldconsents.query;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ApplicationDataItemUserAction implements Displayable {

  VIEW_APPLICATION("View"),
  RESUME_APPLICATION("Resume");

  private final String displayName;

  ApplicationDataItemUserAction(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return this.displayName;
  }
}
