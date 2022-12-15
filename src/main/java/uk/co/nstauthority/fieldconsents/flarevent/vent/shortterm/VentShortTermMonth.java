package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
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

