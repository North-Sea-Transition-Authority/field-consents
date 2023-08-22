package uk.co.nstauthority.fieldconsents.teams;

public enum TeamType {

  REGULATOR("Regulator", 10),
  INDUSTRY("Industry", 20),
  OPRED("OPRED", 30),
  ;

  private final String displayText;
  private final int displayOrder;

  TeamType(String displayText, int displayOrder) {
    this.displayText = displayText;
    this.displayOrder = displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

}
