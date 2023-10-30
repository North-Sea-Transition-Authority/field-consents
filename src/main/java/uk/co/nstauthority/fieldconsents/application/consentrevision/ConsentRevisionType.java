package uk.co.nstauthority.fieldconsents.application.consentrevision;

import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ConsentRevisionType implements Displayable {

  NEW_CONSENT("New consent"),
  REVISION("Revision");

  private final String displayName;

  ConsentRevisionType(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  public static ConsentRevisionType from(Application application) {
    return application.getVariationNo() == 0 ? NEW_CONSENT : REVISION;
  }
}
