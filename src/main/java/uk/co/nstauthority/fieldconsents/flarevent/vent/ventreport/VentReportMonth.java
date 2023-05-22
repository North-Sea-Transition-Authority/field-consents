package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.NoSuchElementException;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Table(name = "vent_report_months")
public class VentReportMonth extends FlareVentRow {

  private Integer shutDownDays;

  static VentReportMonth from(ApplicationVersion applicationVersion,
                              VentReportMonthForm ventReportMonthForm) {
    VentReportMonth ventReportMonth = new VentReportMonth();
    ventReportMonth.setShutDownDays(
        ventReportMonthForm.getShutDownDays().getAsInteger().orElseThrow(NoSuchElementException::new));
    ventReportMonth.updateFlareVentRowFromForm(applicationVersion, ventReportMonthForm);

    return ventReportMonth;
  }

  public Integer getShutDownDays() {
    return shutDownDays;
  }

  public void setShutDownDays(Integer shutDownDays) {
    this.shutDownDays = shutDownDays;
  }

}
