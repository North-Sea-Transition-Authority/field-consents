package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.time.YearMonth;
import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowForm;

public class FlareReportMonthForm extends FlareVentRowForm {

  private final IntegerInput shutDownDays;

  public FlareReportMonthForm() {
    shutDownDays = new IntegerInput("shutDownDays", "days of total shutdown");
  }

  public static FlareReportMonthForm from(YearMonth yearMonth) {
    var flareReportMonthForm = new FlareReportMonthForm();
    flareReportMonthForm.updateFromYearMonth(yearMonth);
    return flareReportMonthForm;
  }

  public static FlareReportMonthForm from(FlareReportMonth flareReportMonth) {
    var flareReportMonthForm = new FlareReportMonthForm();
    flareReportMonthForm.setShutDownDays(flareReportMonth.getShutDownDays().toString());
    flareReportMonthForm.updateFromFlareVentRow(flareReportMonth);
    return flareReportMonthForm;
  }

  public IntegerInput getShutDownDays() {
    return shutDownDays;
  }

  public void setShutDownDays(String shutDownDays) {
    this.shutDownDays.setInputValue(shutDownDays);
  }

}
