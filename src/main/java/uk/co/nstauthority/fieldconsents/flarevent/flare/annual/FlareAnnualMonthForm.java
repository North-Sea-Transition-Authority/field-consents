package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import java.time.YearMonth;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowForm;

public class FlareAnnualMonthForm extends FlareVentRowForm {

  public static FlareAnnualMonthForm from(YearMonth yearMonth) {
    var flareAnnualMonthForm = new FlareAnnualMonthForm();
    flareAnnualMonthForm.updateFromYearMonth(yearMonth);
    return flareAnnualMonthForm;
  }

  public static FlareAnnualMonthForm from(FlareAnnualMonth flareAnnualMonth) {
    var flareAnnualMonthForm = new FlareAnnualMonthForm();
    flareAnnualMonthForm.updateFromFlareVentRow(flareAnnualMonth);
    return flareAnnualMonthForm;
  }

}
