package uk.co.nstauthority.fieldconsents.production;

public enum ProductionUnit {
  SCM_PER_MONTH("scm/month"),
  KSCM_PER_MONTH("kscm/month");

  private final String displayName;

  ProductionUnit(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
