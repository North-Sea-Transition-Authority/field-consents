package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123Row;

@Entity
@Audited
@Table(name = "vent_report_123_months")
public class VentReport123Month extends Vent123Row {

  private Integer shutDownDays;

  public Integer getShutDownDays() {
    return shutDownDays;
  }

  public void setShutDownDays(Integer shutDownDays) {
    this.shutDownDays = shutDownDays;
  }
}
