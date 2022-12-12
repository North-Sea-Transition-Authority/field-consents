package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import java.time.YearMonth;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowForm;

public class VentAnnualMonthForm extends FlareVentRowForm {

  public static VentAnnualMonthForm from(YearMonth yearMonth) {
    var ventAnnualMonthForm = new VentAnnualMonthForm();
    ventAnnualMonthForm.updateFromYearMonth(yearMonth);
    return ventAnnualMonthForm;
  }

  public static VentAnnualMonthForm from(VentAnnualMonth ventAnnualMonth) {
    var ventAnnualMonthForm = new VentAnnualMonthForm();
    ventAnnualMonthForm.updateFromFlareVentRow(ventAnnualMonth);
    return ventAnnualMonthForm;
  }

}
