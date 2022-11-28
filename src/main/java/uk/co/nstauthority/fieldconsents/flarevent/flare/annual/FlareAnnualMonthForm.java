package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowForm;

public class FlareAnnualMonthForm extends FlareVentRowForm {

  private String year;

  private String month;

  private final StringInput comments;

  public FlareAnnualMonthForm() {
    comments = new StringInput("comments", "Comments");
  }
  
  public static FlareAnnualMonthForm from(YearMonth yearMonth) {
    var flareAnnualMonthForm = new FlareAnnualMonthForm();
    flareAnnualMonthForm.setYear(String.valueOf(yearMonth.getYear()));
    flareAnnualMonthForm.setMonth(yearMonth.getMonth());
    return flareAnnualMonthForm;
  }

  public static FlareAnnualMonthForm from(FlareAnnualMonth flareAnnualMonth) {
    var flareAnnualMonthForm = new FlareAnnualMonthForm();
    flareAnnualMonthForm.setYear(flareAnnualMonth.getYear().toString());
    flareAnnualMonthForm.setMonth(flareAnnualMonth.getMonth());
    flareAnnualMonthForm.setComments(flareAnnualMonth.getComments());
    flareAnnualMonthForm.updateFromFlareVentRow(flareAnnualMonth);
    return flareAnnualMonthForm;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public void setMonth(Month month) {
    this.month = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
  }

  public StringInput getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments.setInputValue(comments);
  }

  public YearMonth getYearMonth() {
    return YearMonth.of(Integer.parseInt(this.year), Month.valueOf(this.month.toUpperCase()));
  }
  
}
