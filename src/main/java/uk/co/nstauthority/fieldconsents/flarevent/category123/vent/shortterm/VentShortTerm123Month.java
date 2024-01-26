package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123Row;

@Entity
@Table(name = "vent_short_term_123_months")
public class VentShortTerm123Month extends Vent123Row {

  private LocalDate startDate;

  private LocalDate endDate;

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
