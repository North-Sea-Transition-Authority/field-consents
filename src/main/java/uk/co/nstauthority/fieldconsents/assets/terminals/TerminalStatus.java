package uk.co.nstauthority.fieldconsents.assets.terminals;

import uk.co.fivium.energyportalapi.generated.types.Terminal;

public enum TerminalStatus {
  ACTIVE("Active"),
  INACTIVE("Inactive");

  private final String displayName;

  static TerminalStatus from(Terminal terminal) {
    return Boolean.TRUE.equals(terminal.getTerminalActive()) ? ACTIVE : INACTIVE;
  }

  TerminalStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}