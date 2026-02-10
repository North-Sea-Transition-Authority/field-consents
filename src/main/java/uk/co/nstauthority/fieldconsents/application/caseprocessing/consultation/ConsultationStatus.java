package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ConsultationStatus implements Displayable {
  OPEN("Open"),
  CLOSED("Closed");

  private final String displayName;

  ConsultationStatus(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }
}
