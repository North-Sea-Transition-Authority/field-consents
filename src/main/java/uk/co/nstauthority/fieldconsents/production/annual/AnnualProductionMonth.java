package uk.co.nstauthority.fieldconsents.production.annual;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Month;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;

@Entity
@Table(name = "annual_production_months")
public class AnnualProductionMonth extends ProductionRow {

  private Integer year;

  @Enumerated(EnumType.STRING)
  private Month month;

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
}
