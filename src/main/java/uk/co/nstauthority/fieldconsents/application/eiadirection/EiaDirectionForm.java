package uk.co.nstauthority.fieldconsents.application.eiadirection;

import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class EiaDirectionForm {

  private Boolean haveSubmittedEiaDirection;

  private Integer satId;

  private Boolean haveEiaDirectionToSubmit;

  private final ThreeFieldDateInput latestDateToBeSubmitted;

  private final StringInput whyNoEiaDirection;

  public static EiaDirectionForm from(EiaDirection eiaDirection) {
    var eiaDirectionForm = new EiaDirectionForm();
    eiaDirectionForm.setHaveSubmittedEiaDirection(eiaDirection.getHaveSubmittedEiaDirection());
    eiaDirectionForm.setSatId(eiaDirection.getSatId());
    eiaDirectionForm.setHaveEiaDirectionToSubmit(eiaDirection.getHaveEiaDirectionToSubmit());
    if (eiaDirection.getLatestDateToBeSubmitted() != null) {
      eiaDirectionForm.getLatestDateToBeSubmitted().setDate(eiaDirection.getLatestDateToBeSubmitted());
    }
    eiaDirectionForm.getWhyNoEiaDirection().setInputValue(eiaDirection.getWhyNoEiaDirection());
    return eiaDirectionForm;
  }

  public EiaDirectionForm() {
    latestDateToBeSubmitted = new ThreeFieldDateInput("latestDateToBeSubmitted", "submission date");
    whyNoEiaDirection = new StringInput("whyNoEiaDirection", "an explanation");
  }

  public Boolean getHaveSubmittedEiaDirection() {
    return haveSubmittedEiaDirection;
  }

  public void setHaveSubmittedEiaDirection(Boolean haveSubmittedEiaDirection) {
    this.haveSubmittedEiaDirection = haveSubmittedEiaDirection;
  }

  public Integer getSatId() {
    return satId;
  }

  public void setSatId(Integer satId) {
    this.satId = satId;
  }

  public Boolean getHaveEiaDirectionToSubmit() {
    return haveEiaDirectionToSubmit;
  }

  public void setHaveEiaDirectionToSubmit(Boolean haveEiaDirectionToSubmit) {
    this.haveEiaDirectionToSubmit = haveEiaDirectionToSubmit;
  }

  public ThreeFieldDateInput getLatestDateToBeSubmitted() {
    return latestDateToBeSubmitted;
  }

  public StringInput getWhyNoEiaDirection() {
    return whyNoEiaDirection;
  }
}
