package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import java.time.Month;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRow;

@Entity
@Table(name = "flare_annual_months")
class FlareAnnualMonth extends FlareVentRow {

  private Integer year;

  @Enumerated(EnumType.STRING)
  private Month month;

  private String comments;

  static FlareAnnualMonth from(ApplicationVersion applicationVersion,
                               FlareAnnualMonthForm flareAnnualMonthForm) {
    FlareAnnualMonth flareAnnualMonth = new FlareAnnualMonth();
    flareAnnualMonth.setApplicationVersion(applicationVersion);
    flareAnnualMonth.setYear(Integer.parseInt(flareAnnualMonthForm.getYear()));
    flareAnnualMonth.setMonth(Month.valueOf(flareAnnualMonthForm.getMonth().toUpperCase()));
    flareAnnualMonth.setComments(flareAnnualMonthForm.getComments().getInputValue());
    flareAnnualMonth.updateFlareVentRowFromForm(flareAnnualMonthForm);

    return flareAnnualMonth;
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

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

}

