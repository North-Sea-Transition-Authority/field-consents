package uk.co.nstauthority.fieldconsents.flarevent;

import java.time.Month;
import java.time.YearMonth;
import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

public class FlareVentRowForm {

  private String year;

  private String month;

  private final DecimalInput categoryA;

  private final DecimalInput categoryB;

  private final DecimalInput categoryC;

  private final StringInput comments;

  public FlareVentRowForm() {
    categoryA = new DecimalInput("categoryA", "Category A");
    categoryB = new DecimalInput("categoryB", "Category B");
    categoryC = new DecimalInput("categoryC", "Category C");
    comments = new StringInput("comments", "Comments");
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
    this.month = DateUtils.formatFull(month);
  }

  public DecimalInput getCategoryA() {
    return categoryA;
  }

  public void setCategoryA(String categoryA) {
    this.categoryA.setInputValue(categoryA);
  }

  public DecimalInput getCategoryB() {
    return categoryB;
  }

  public void setCategoryB(String categoryB) {
    this.categoryB.setInputValue(categoryB);
  }

  public DecimalInput getCategoryC() {
    return categoryC;
  }

  public void setCategoryC(String categoryC) {
    this.categoryC.setInputValue(categoryC);
  }

  public StringInput getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments.setInputValue(comments);
  }

  public void updateFromYearMonth(YearMonth yearMonth) {
    setYear(String.valueOf(yearMonth.getYear()));
    setMonth(yearMonth.getMonth());
  }

  public void updateFromFlareVentRow(FlareVentRow flareVentRow) {
    setYear(String.valueOf(flareVentRow.getYear()));
    setMonth(flareVentRow.getMonth());
    setCategoryA(DecimalFormatUtils.bigDecimalToFormattedString(flareVentRow.getCategoryA()));
    setCategoryB(DecimalFormatUtils.bigDecimalToFormattedString(flareVentRow.getCategoryB()));
    setCategoryC(DecimalFormatUtils.bigDecimalToFormattedString(flareVentRow.getCategoryC()));
    setComments(flareVentRow.getComments());
  }

  public YearMonth getYearMonth() {
    return YearMonth.of(Integer.parseInt(this.year), Month.valueOf(this.month.toUpperCase()));
  }

  public Integer getMonthDays() {
    return getYearMonth().lengthOfMonth();
  }

}
