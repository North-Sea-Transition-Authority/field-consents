package uk.co.nstauthority.fieldconsents.application.consentlength;

import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;

public class ConsentLengthForm {

  private ConsentLengthType consentLengthType;

  private ThreeFieldDateInput shortTermStartDate;

  private ThreeFieldDateInput shortTermEndDate;

  private final IntegerInput annualConsentYear;

  private final IntegerInput longTermStartYear;

  private final IntegerInput longTermEndYear;

  public ConsentLengthForm() {
    // TODO DFL-32 update below when DFL updated
    shortTermStartDate = new ThreeFieldDateInput("shortTermStartDate", "Start date",
        new IntegerInput("shortTermStartDay", "Day"),
        new IntegerInput("shortTermStartMonth", "Month"),
        new IntegerInput("shortTermStartYear", "Year")
    );
    shortTermEndDate = new ThreeFieldDateInput("shortTermEndDate", "End date",
        new IntegerInput("shortTermEndDay", "Day"),
        new IntegerInput("shortTermEndMonth", "Month"),
        new IntegerInput("shortTermEndYear", "Year")
    );
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

  public IntegerInput getShortTermStartDay() {
    return shortTermStartDate.getDayInputValue();
  }

  public void setShortTermStartDay(String shortTermStartDay) {
    this.shortTermStartDate.getDayInputValue().setInputValue(shortTermStartDay);
  }

  public IntegerInput getShortTermStartMonth() {
    return shortTermStartDate.getMonthInputValue();
  }

  public void setShortTermStartMonth(String shortTermStartMonth) {
    this.shortTermStartDate.getMonthInputValue().setInputValue(shortTermStartMonth);
  }

  public IntegerInput getShortTermStartYear() {
    return shortTermStartDate.getYearInputValue();
  }

  public void setShortTermStartYear(String shortTermStartYear) {
    this.shortTermStartDate.getYearInputValue().setInputValue(shortTermStartYear);
  }

  public IntegerInput getShortTermEndDay() {
    return shortTermEndDate.getDayInputValue();
  }

  public void setShortTermEndDay(String shortTermEndDay) {
    this.shortTermEndDate.getDayInputValue().setInputValue(shortTermEndDay);
  }

  public IntegerInput getShortTermEndMonth() {
    return shortTermEndDate.getMonthInputValue();
  }

  public void setShortTermEndMonth(String shortTermEndMonth) {
    this.shortTermEndDate.getMonthInputValue().setInputValue(shortTermEndMonth);
  }

  public IntegerInput getShortTermEndYear() {
    return shortTermEndDate.getYearInputValue();
  }

  public void setShortTermEndYear(String shortTermEndYear) {
    this.shortTermEndDate.getYearInputValue().setInputValue(shortTermEndYear);
  }

  public IntegerInput getAnnualConsentYear() {
    return annualConsentYear;
  }

  public void setAnnualConsentYear(String annualConsentYear) {
    this.annualConsentYear.setInputValue(annualConsentYear);
  }

  public IntegerInput getLongTermStartYear() {
    return longTermStartYear;
  }

  public void setLongTermStartYear(String longTermStartYear) {
    this.longTermStartYear.setInputValue(longTermStartYear);
  }

  public IntegerInput getLongTermEndYear() {
    return longTermEndYear;
  }

  public void setLongTermEndYear(String longTermEndYear) {
    this.longTermEndYear.setInputValue(longTermEndYear);
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
