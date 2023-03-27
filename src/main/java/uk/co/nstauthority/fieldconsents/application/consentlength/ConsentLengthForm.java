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
    shortTermStartDate = new ThreeFieldDateInput("shortTermStartDate", "start date");
    shortTermEndDate = new ThreeFieldDateInput("shortTermEndDate", "end date");
    annualConsentYear = new IntegerInput("annualConsentYear", "a year");
    longTermStartYear = new IntegerInput("longTermStartYear", "a start year");
    longTermEndYear = new IntegerInput("longTermEndYear", "an end year");
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
