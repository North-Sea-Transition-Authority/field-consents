package uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted;

import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirection;

public record HaveSubmittedForm(
    Boolean haveSubmittedEiaDirection,
    Integer satId
) {

  public static HaveSubmittedForm empty() {
    return new HaveSubmittedForm(null, null);
  }

  public static HaveSubmittedForm from(EiaDirection eiaDirection) {
    return new HaveSubmittedForm(eiaDirection.getHaveSubmittedEiaDirection(), eiaDirection.getSatId());
  }

}
