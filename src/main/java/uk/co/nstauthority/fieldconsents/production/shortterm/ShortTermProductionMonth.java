package uk.co.nstauthority.fieldconsents.production.shortterm;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.Month;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;

@Entity
@Table(name = "short_term_production_months")
public class ShortTermProductionMonth extends ProductionRow {

  private Integer year;

  @Enumerated(EnumType.STRING)
  private Month month;

  private LocalDate startDate;

  private LocalDate endDate;


  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Month getMonth() {
    return month;
  }

  public void setMonth(Month month) {
    this.month = month;
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
