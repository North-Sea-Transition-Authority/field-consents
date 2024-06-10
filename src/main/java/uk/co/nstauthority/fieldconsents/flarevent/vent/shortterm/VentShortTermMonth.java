package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Audited
@Table(name = "vent_short_term_months")
public class VentShortTermMonth extends FlareVentRow {

  private LocalDate startDate;

  private LocalDate endDate;

  static VentShortTermMonth from(ApplicationVersion applicationVersion,
                                 VentShortTermMonthForm ventShortTermMonthForm) {
    VentShortTermMonth ventShortTermMonth = new VentShortTermMonth();
    ventShortTermMonth.setStartDate(ventShortTermMonthForm.getStartDate());
    ventShortTermMonth.setEndDate(ventShortTermMonthForm.getEndDate());
    ventShortTermMonth.updateFlareVentRowFromForm(applicationVersion, ventShortTermMonthForm);

    return ventShortTermMonth;
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

}
