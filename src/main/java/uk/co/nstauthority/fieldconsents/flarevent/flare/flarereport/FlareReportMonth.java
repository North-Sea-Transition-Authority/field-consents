package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.NoSuchElementException;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Table(name = "flare_report_months")
public class FlareReportMonth extends FlareVentRow {

  private Integer shutDownDays;

  static FlareReportMonth from(ApplicationVersion applicationVersion,
                               FlareReportMonthForm flareReportMonthForm) {
    FlareReportMonth flareReportMonth = new FlareReportMonth();
    flareReportMonth.setShutDownDays(
        flareReportMonthForm.getShutDownDays().getAsInteger().orElseThrow(NoSuchElementException::new));
    flareReportMonth.updateFlareVentRowFromForm(applicationVersion, flareReportMonthForm);

    return flareReportMonth;
  }

  public Integer getShutDownDays() {
    return shutDownDays;
  }

  public void setShutDownDays(Integer shutDownDays) {
    this.shutDownDays = shutDownDays;
  }

}
