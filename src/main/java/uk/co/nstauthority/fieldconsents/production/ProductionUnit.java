package uk.co.nstauthority.fieldconsents.production;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ProductionUnit implements Displayable {
  SCM_PER_DAY("scm/day"),
  KSCM_PER_DAY("kscm/day"),
  SCM_PER_MONTH("scm/month"),
  KSCM_PER_MONTH("kscm/month");

  private final String displayName;

  ProductionUnit(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }
}
