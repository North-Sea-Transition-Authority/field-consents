package uk.co.nstauthority.fieldconsents.production.longterm;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;

@Entity
@Table(name = "long_term_production_years")
public class LongTermProductionYear extends ProductionRow {

  private Integer year;

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }
}
