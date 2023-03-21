package uk.co.nstauthority.fieldconsents.production.shortterm;

import java.time.LocalDate;
import java.time.Month;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRowForm;

public class ShortTermProductionMonthForm extends ProductionRowForm {

  private String month;

  private String year;

  private LocalDate startDate;

  private LocalDate endDate;

  private Integer consentDays;

  public ShortTermProductionMonthForm() {
  }

  public ShortTermProductionMonthForm(String month, String year, int consentDays,
                                      LocalDate startDate, LocalDate endDate,
                                      DecimalInput oilMinValue,
                                      DecimalInput oilMaxValue,
                                      DecimalInput gasMinValue,
                                      DecimalInput gasMaxValue) {
    super(oilMinValue, oilMaxValue, gasMinValue, gasMaxValue);
    this.month = month;
    this.year = year;
    this.startDate = startDate;
    this.endDate = endDate;
    this.consentDays = consentDays;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public void setMonth(Month month) {
    this.month = DateUtils.formatFull(month);
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public Integer getConsentDays() {
    return consentDays;
  }

  public void setConsentDays(Integer consentDays) {
    this.consentDays = consentDays;
  }

  public Pair<LocalDate, LocalDate> getMonthTerm() {
    return Pair.of(this.startDate, this.endDate);
  }
}
