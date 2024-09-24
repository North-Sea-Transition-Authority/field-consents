package uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose;

import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirection;

public record ProjectPurposeForm(
    Boolean forPurposeOfEiaRegs,
    StringInput rationaleForPurposeOfEiaRegs,
    StringInput rationaleNotForPurposeOfEiaRegs
) {

  public ProjectPurposeForm {
    rationaleForPurposeOfEiaRegs = new StringInput(
        "rationaleForPurposeOfEiaRegs",
        "why this is a \"project\""
    );
    rationaleNotForPurposeOfEiaRegs = new StringInput(
        "rationaleNotForPurposeOfEiaRegs",
        "why this is not a \"project\""
    );
  }

  public StringInput getRationaleForPurposeOfEiaRegs() {
    return rationaleForPurposeOfEiaRegs;
  }

  public void setRationaleForPurposeOfEiaRegs(String rationaleNotForPurposeOfEiaRegs) {
    this.rationaleForPurposeOfEiaRegs.setInputValue(rationaleNotForPurposeOfEiaRegs);
  }

  public StringInput getRationaleNotForPurposeOfEiaRegs() {
    return rationaleNotForPurposeOfEiaRegs;
  }

  public void setRationaleNotForPurposeOfEiaRegs(String rationaleNotForPurposeOfEiaRegs) {
    this.rationaleNotForPurposeOfEiaRegs.setInputValue(rationaleNotForPurposeOfEiaRegs);
  }

  public static ProjectPurposeForm from(EiaDirection eiaDirection) {
    var form = new ProjectPurposeForm(eiaDirection.getForPurposeOfEiaRegs(), null, null);
    if (Boolean.TRUE.equals(eiaDirection.getForPurposeOfEiaRegs())) {
      form.rationaleForPurposeOfEiaRegs.setInputValue(eiaDirection.getRationaleForPurposeOfEiaRegs());
    }
    if (Boolean.FALSE.equals(eiaDirection.getForPurposeOfEiaRegs())) {
      form.rationaleNotForPurposeOfEiaRegs.setInputValue(eiaDirection.getRationaleForPurposeOfEiaRegs());
    }
    return form;
  }

  public static ProjectPurposeForm empty() {
    return new ProjectPurposeForm(null, null, null);
  }
}
