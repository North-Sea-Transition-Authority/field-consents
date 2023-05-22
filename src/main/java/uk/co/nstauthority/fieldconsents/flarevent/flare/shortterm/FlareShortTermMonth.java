package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Table(name = "flare_short_term_months")
public class FlareShortTermMonth extends FlareVentRow {

  private LocalDate startDate;

  private LocalDate endDate;

  static FlareShortTermMonth from(ApplicationVersion applicationVersion,
                                  FlareShortTermMonthForm flareShortTermMonthForm) {
    FlareShortTermMonth flareShortTermMonth = new FlareShortTermMonth();
    flareShortTermMonth.setStartDate(flareShortTermMonthForm.getStartDate());
    flareShortTermMonth.setEndDate(flareShortTermMonthForm.getEndDate());
    flareShortTermMonth.updateFlareVentRowFromForm(applicationVersion, flareShortTermMonthForm);

    return flareShortTermMonth;
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
