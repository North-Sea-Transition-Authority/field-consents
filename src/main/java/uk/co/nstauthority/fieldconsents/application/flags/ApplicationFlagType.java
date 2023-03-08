package uk.co.nstauthority.fieldconsents.application.flags;

public enum ApplicationFlagType {
  HAS_SECONDARY_ASSETS("Do you have any additional fields to add?"),
  WILL_GAS_BE_INJECTED("Will gas be injected for the purpose of creating or increasing the pressure support?");

  private final String displayName;

  ApplicationFlagType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}