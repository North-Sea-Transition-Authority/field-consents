package uk.co.nstauthority.fieldconsents.application.flags;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ApplicationFlagType implements Displayable {
  HAS_SECONDARY_ASSETS("Do you have any additional fields to add?"),
  WILL_GAS_BE_INJECTED("Will gas be injected for the purpose of creating or increasing pressure support?"),
  IS_ACE_APPLICATION("Is this an ACE application?");

  private final String displayName;

  ApplicationFlagType(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }
}
