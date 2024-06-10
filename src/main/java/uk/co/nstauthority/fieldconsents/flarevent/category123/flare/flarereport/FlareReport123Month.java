package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123Row;

@Entity
@Audited
@Table(name = "flare_report_123_months")
public class FlareReport123Month extends Flare123Row {

  private Integer shutDownDays;

  public Integer getShutDownDays() {
    return shutDownDays;
  }

  public void setShutDownDays(Integer shutDownDays) {
    this.shutDownDays = shutDownDays;
  }
}
