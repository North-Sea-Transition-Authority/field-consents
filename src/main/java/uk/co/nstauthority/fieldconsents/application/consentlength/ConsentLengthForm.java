package uk.co.nstauthority.fieldconsents.application.consentlength;

import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class ConsentLengthForm {

  private ConsentLengthType consentLengthType;

  private final ThreeFieldDateInput shortTermStartDate;

  private final ThreeFieldDateInput shortTermEndDate;

  private final IntegerInput annualConsentYear;

  private final IntegerInput longTermStartYear;

  private final IntegerInput longTermEndYear;

  public ConsentLengthForm() {
    shortTermStartDate = new ThreeFieldDateInput("shortTermStartDate", "Start date");
    shortTermEndDate = new ThreeFieldDateInput("shortTermEndDate", "End date");
    annualConsentYear = new IntegerInput("annualConsentYear", "Year");
    longTermStartYear = new IntegerInput("longTermStartYear", "Start year");
    longTermEndYear = new IntegerInput("longTermEndYear", "End year");
  }

  public ConsentLengthType getConsentLengthType() {
    return consentLengthType;
  }

  public void setConsentLengthType(ConsentLengthType consentLengthType) {
    this.consentLengthType = consentLengthType;
  }

  public IntegerInput getAnnualConsentYear() {
    return annualConsentYear;
  }

  public IntegerInput getLongTermStartYear() {
    return longTermStartYear;
  }

  public IntegerInput getLongTermEndYear() {
    return longTermEndYear;
  }

  public ThreeFieldDateInput getShortTermStartDate() {
    return shortTermStartDate;
  }

  public ThreeFieldDateInput getShortTermEndDate() {
    return shortTermEndDate;
  }

}
