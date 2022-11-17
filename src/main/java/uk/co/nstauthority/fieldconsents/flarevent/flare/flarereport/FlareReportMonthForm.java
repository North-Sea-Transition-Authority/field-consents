package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import uk.co.fivium.formlibrary.input.IntegerInput;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowForm;

public class FlareReportMonthForm extends FlareVentRowForm {

  private String year;

  private String month;

  private final IntegerInput shutDownDays;

  private final StringInput comments;

  public FlareReportMonthForm() {
    shutDownDays = new IntegerInput("shutDownDays", "Days of total shutdown");
    comments = new StringInput("comments", "Comments");
  }

  public static FlareReportMonthForm from(YearMonth yearMonth) {
    var flareReportMonthForm = new FlareReportMonthForm();
    flareReportMonthForm.setYear(String.valueOf(yearMonth.getYear()));
    flareReportMonthForm.setMonth(yearMonth.getMonth());
    return flareReportMonthForm;
  }

  public static FlareReportMonthForm from(FlareReportMonth flareReportMonth) {
    var flareReportMonthForm = new FlareReportMonthForm();
    flareReportMonthForm.setYear(flareReportMonth.getYear().toString());
    flareReportMonthForm.setMonth(flareReportMonth.getMonth());
    flareReportMonthForm.setShutDownDays(flareReportMonth.getShutDownDays().toString());
    flareReportMonthForm.setComments(flareReportMonth.getComments());
    flareReportMonthForm.updateFromFlareVentRow(flareReportMonth);
    return flareReportMonthForm;
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

  public IntegerInput getShutDownDays() {
    return shutDownDays;
  }

  public void setShutDownDays(String shutDownDays) {
    this.shutDownDays.setInputValue(shutDownDays);
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

  public Integer getMonthDays() {
    return getYearMonth().lengthOfMonth();
  }

}
