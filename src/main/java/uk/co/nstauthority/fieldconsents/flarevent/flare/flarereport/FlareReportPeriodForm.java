package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.fivium.formlibrary.input.StringInput;

public class FlareReportPeriodForm {

  private Boolean hasDataForPeriod;

  private final StringInput reportEndMonth;

  private final IntegerInput reportEndYear;

  FlareReportPeriodForm() {
    reportEndMonth = new StringInput("reportEndMonth", "Month");
    reportEndYear = new IntegerInput("reportEndYear", "Year");
  }

  static FlareReportPeriodForm from(FlareReportPeriod flareReportPeriod) {
    FlareReportPeriodForm flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.setHasDataForPeriod(flareReportPeriod.getHasDataForPeriod());
    flareReportPeriodForm.setReportEndMonth(flareReportPeriod.getReportEndMonth().name());
    flareReportPeriodForm.setReportEndYear(flareReportPeriod.getReportEndYear().toString());
    return flareReportPeriodForm;
  }

  public Boolean getHasDataForPeriod() {
    return hasDataForPeriod;
  }

  public void setHasDataForPeriod(Boolean hasDataForPeriod) {
    this.hasDataForPeriod = hasDataForPeriod;
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
