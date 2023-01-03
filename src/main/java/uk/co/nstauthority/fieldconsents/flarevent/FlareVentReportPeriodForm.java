package uk.co.nstauthority.fieldconsents.flarevent;

import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.fivium.formlibrary.input.StringInput;

public class FlareVentReportPeriodForm {

  private final StringInput reportEndMonth;

  private final IntegerInput reportEndYear;

  public FlareVentReportPeriodForm() {
    reportEndMonth = new StringInput("reportEndMonth", "Month");
    reportEndYear = new IntegerInput("reportEndYear", "Year");
  }

  public static FlareVentReportPeriodForm from(FlareVentReportPeriod flareVentReportPeriod) {
    FlareVentReportPeriodForm flareVentReportPeriodForm = new FlareVentReportPeriodForm();
    flareVentReportPeriodForm.setReportEndMonth(flareVentReportPeriod.getReportEndMonth().name());
    flareVentReportPeriodForm.setReportEndYear(flareVentReportPeriod.getReportEndYear().toString());
    return flareVentReportPeriodForm;
  }

  public StringInput getReportEndMonth() {
    return reportEndMonth;
  }

  public void setReportEndMonth(String reportEndMonth) {
    this.reportEndMonth.setInputValue(reportEndMonth);
  }

  public IntegerInput getReportEndYear() {
    return reportEndYear;
  }

  public void setReportEndYear(String reportEndYear) {
    this.reportEndYear.setInputValue(reportEndYear);
  }
}
