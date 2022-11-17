package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.time.Month;
import java.util.NoSuchElementException;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Table(name = "flare_report_months")
class FlareReportMonth extends FlareVentRow {

  private Integer year;

  @Enumerated(EnumType.STRING)
  private Month month;

  private Integer shutDownDays;

  private String comments;

  static FlareReportMonth from(ApplicationVersion applicationVersion,
                               FlareReportMonthForm flareReportMonthForm) {
    FlareReportMonth flareReportMonth = new FlareReportMonth();
    flareReportMonth.setApplicationVersion(applicationVersion);
    flareReportMonth.setYear(Integer.parseInt(flareReportMonthForm.getYear()));
    flareReportMonth.setMonth(Month.valueOf(flareReportMonthForm.getMonth().toUpperCase()));
    flareReportMonth.setShutDownDays(
        flareReportMonthForm.getShutDownDays().getAsInteger().orElseThrow(NoSuchElementException::new));
    flareReportMonth.setComments(flareReportMonthForm.getComments().getInputValue());
    flareReportMonth.updateFlareVentRowFromForm(flareReportMonthForm);

    return flareReportMonth;
  }

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

  public Integer getShutDownDays() {
    return shutDownDays;
  }

  public void setShutDownDays(Integer shutDownDays) {
    this.shutDownDays = shutDownDays;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

}
