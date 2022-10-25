package uk.co.nstauthority.fieldconsents.application.consentlength;

import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class ConsentLengthForm {

  private ConsentLengthType consentLengthType;

  private ThreeFieldDateInput shortTermStartDate =
      new ThreeFieldDateInput("shortTermStartDate", "Start date",
          new IntegerInput("shortTermStartDay", "Day"),
          new IntegerInput("shortTermStartMonth", "Month"),
          new IntegerInput("shortTermStartYear", "Year")
      );

  private ThreeFieldDateInput shortTermEndDate =
      new ThreeFieldDateInput("shortTermEndDate", "End date",
          new IntegerInput("shortTermEndDay", "Day"),
          new IntegerInput("shortTermEndMonth", "Month"),
          new IntegerInput("shortTermEndYear", "Year")
      );

  private IntegerInput annualConsentYear = new IntegerInput("annualConsentYear", "Year");

  private IntegerInput longTermStartYear = new IntegerInput("longTermStartYear", "Start year");

  private IntegerInput longTermEndYear = new IntegerInput("longTermEndYear", "End year");


  public ConsentLengthType getConsentLengthType() {
    return consentLengthType;
  }

  public void setConsentLengthType(ConsentLengthType consentLengthType) {
    this.consentLengthType = consentLengthType;
  }

  public IntegerInput getShortTermStartDay() {
    return shortTermStartDate.getDayInputValue();
  }

  public void setShortTermStartDay(IntegerInput shortTermStartDay) {
    this.shortTermStartDate.setDayInputValue(shortTermStartDay);
  }

  public IntegerInput getShortTermStartMonth() {
    return shortTermStartDate.getMonthInputValue();
  }

  public void setShortTermStartMonth(IntegerInput shortTermStartMonth) {
    this.shortTermStartDate.setMonthInputValue(shortTermStartMonth);
  }

  public IntegerInput getShortTermStartYear() {
    return shortTermStartDate.getYearInputValue();
  }

  public void setShortTermStartYear(IntegerInput shortTermStartYear) {
    this.shortTermStartDate.setYearInputValue(shortTermStartYear);
  }

  public IntegerInput getShortTermEndDay() {
    return shortTermEndDate.getDayInputValue();
  }

  public void setShortTermEndDay(IntegerInput shortTermEndDay) {
    this.shortTermEndDate.setDayInputValue(shortTermEndDay);
  }

  public IntegerInput getShortTermEndMonth() {
    return shortTermEndDate.getMonthInputValue();
  }

  public void setShortTermEndMonth(IntegerInput shortTermEndMonth) {
    this.shortTermEndDate.setMonthInputValue(shortTermEndMonth);
  }

  public IntegerInput getShortTermEndYear() {
    return shortTermEndDate.getYearInputValue();
  }

  public void setShortTermEndYear(IntegerInput shortTermEndYear) {
    this.shortTermEndDate.setYearInputValue(shortTermEndYear);
  }

  public IntegerInput getAnnualConsentYear() {
    return annualConsentYear;
  }

  public void setAnnualConsentYear(IntegerInput annualConsentYear) {
    this.annualConsentYear = annualConsentYear;
  }

  public IntegerInput getLongTermStartYear() {
    return longTermStartYear;
  }

  public void setLongTermStartYear(IntegerInput longTermStartYear) {
    this.longTermStartYear = longTermStartYear;
  }

  public IntegerInput getLongTermEndYear() {
    return longTermEndYear;
  }

  public void setLongTermEndYear(IntegerInput longTermEndYear) {
    this.longTermEndYear = longTermEndYear;
  }

  public ThreeFieldDateInput getShortTermStartDate() {
    return shortTermStartDate;
  }

  public void setShortTermStartDate(ThreeFieldDateInput shortTermStartDate) {
    this.shortTermStartDate = shortTermStartDate;
  }

  public ThreeFieldDateInput getShortTermEndDate() {
    return shortTermEndDate;
  }

  public void setShortTermEndDate(ThreeFieldDateInput shortTermEndDate) {
    this.shortTermEndDate = shortTermEndDate;
  }
}
