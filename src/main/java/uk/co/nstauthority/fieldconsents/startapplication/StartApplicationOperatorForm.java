package uk.co.nstauthority.fieldconsents.startapplication;

import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

class StartApplicationOperatorForm extends StartApplicationForm {

  private final IntegerInput organisationUnitId;

  public StartApplicationOperatorForm(ApplicationType applicationType) {
    super(applicationType);
    this.organisationUnitId = new IntegerInput("organisationUnitId", "an operator");
  }

  public IntegerInput getOrganisationUnitId() {
    return organisationUnitId;
  }

  public void setOrganisationUnitId(String organisationUnitId) {
    this.organisationUnitId.setInputValue(organisationUnitId);
  }

}
