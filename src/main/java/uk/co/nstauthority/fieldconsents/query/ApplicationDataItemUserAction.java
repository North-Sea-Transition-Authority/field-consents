package uk.co.nstauthority.fieldconsents.query;

public enum ApplicationDataItemUserAction {

  VIEW_APPLICATION("View"),
  RESUME_APPLICATION("Resume");

  private final String displayName;

  ApplicationDataItemUserAction(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return this.displayName;
  }
}
