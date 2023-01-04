package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.time.YearMonth;
import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowForm;

public class VentReportMonthForm extends FlareVentRowForm {

  private final IntegerInput shutDownDays;

  public VentReportMonthForm() {
    shutDownDays = new IntegerInput("shutDownDays", "Days of total shutdown");
  }

  public static VentReportMonthForm from(YearMonth yearMonth) {
    var ventReportMonthForm = new VentReportMonthForm();
    ventReportMonthForm.updateFromYearMonth(yearMonth);
    return ventReportMonthForm;
  }

  public static VentReportMonthForm from(VentReportMonth ventReportMonth) {
    var ventReportMonthForm = new VentReportMonthForm();
    ventReportMonthForm.setShutDownDays(ventReportMonth.getShutDownDays().toString());
    ventReportMonthForm.updateFromFlareVentRow(ventReportMonth);
    return ventReportMonthForm;
  }

  public IntegerInput getShutDownDays() {
    return shutDownDays;
  }

  public void setShutDownDays(String shutDownDays) {
    this.shutDownDays.setInputValue(shutDownDays);
  }

}
