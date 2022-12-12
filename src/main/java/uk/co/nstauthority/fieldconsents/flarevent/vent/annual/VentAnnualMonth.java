package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import javax.persistence.Entity;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Table(name = "vent_annual_months")
public class VentAnnualMonth extends FlareVentRow {

  static VentAnnualMonth from(ApplicationVersion applicationVersion,
                              VentAnnualMonthForm ventAnnualMonthForm) {
    VentAnnualMonth ventAnnualMonth = new VentAnnualMonth();
    ventAnnualMonth.updateFlareVentRowFromForm(applicationVersion, ventAnnualMonthForm);

    return ventAnnualMonth;
  }

}