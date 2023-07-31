package uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose;

import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirection;

public record ProjectPurposeForm(Boolean forPurposeOfEiaRegs) {

  public static ProjectPurposeForm from(EiaDirection eiaDirection) {
    return new ProjectPurposeForm(eiaDirection.getForPurposeOfEiaRegs());
  }

  public static ProjectPurposeForm empty() {
    return new ProjectPurposeForm(null);
  }

}
