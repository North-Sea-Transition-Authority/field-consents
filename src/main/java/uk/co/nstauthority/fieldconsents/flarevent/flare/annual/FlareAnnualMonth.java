package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Audited
@Table(name = "flare_annual_months")
public class FlareAnnualMonth extends FlareVentRow {

  static FlareAnnualMonth from(ApplicationVersion applicationVersion,
                               FlareAnnualMonthForm flareAnnualMonthForm) {
    FlareAnnualMonth flareAnnualMonth = new FlareAnnualMonth();
    flareAnnualMonth.updateFlareVentRowFromForm(applicationVersion, flareAnnualMonthForm);

    return flareAnnualMonth;
  }

}
