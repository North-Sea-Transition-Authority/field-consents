package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum TechnicalReviewStatus implements Displayable {
  OPEN("Open"),
  CLOSED("Closed");

  TechnicalReviewStatus(String displayName) {
    this.displayName = displayName;
  }

  private final String displayName;

  @Override
  public String getDisplayName() {
    return displayName;
  }
}
