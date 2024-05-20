package uk.co.nstauthority.fieldconsents.application.caseprocessing.revision;

import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ApplicationRevisionType implements Displayable {

  NEW_CONSENT("New consent"),
  REVISION("Revision");

  private final String displayName;

  ApplicationRevisionType(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  public static ApplicationRevisionType from(Application application) {
    return application.getVariationNo() == 0 ? NEW_CONSENT : REVISION;
  }
}
