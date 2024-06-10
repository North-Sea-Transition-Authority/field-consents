package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Audited
@Table(name = "vent_annual_months")
public class VentAnnualMonth extends FlareVentRow {

  static VentAnnualMonth from(ApplicationVersion applicationVersion,
                              VentAnnualMonthForm ventAnnualMonthForm) {
    VentAnnualMonth ventAnnualMonth = new VentAnnualMonth();
    ventAnnualMonth.updateFlareVentRowFromForm(applicationVersion, ventAnnualMonthForm);

    return ventAnnualMonth;
  }

}
