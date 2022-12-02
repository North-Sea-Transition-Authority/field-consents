package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import java.time.LocalDate;
import java.time.YearMonth;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowForm;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class FlareShortTermMonthForm extends FlareVentRowForm {

  private LocalDate startDate;

  private LocalDate endDate;

  private Integer consentDays;

  public static FlareShortTermMonthForm from(LocalDate startDate, LocalDate endDate) {
    var flareShortTermMonthForm = new FlareShortTermMonthForm();
    flareShortTermMonthForm.setStartDate(startDate);
    flareShortTermMonthForm.setEndDate(endDate);
    flareShortTermMonthForm.setConsentDays(DateUtils.daysBetweenInclusive(startDate, endDate));
    flareShortTermMonthForm.updateFromYearMonth(YearMonth.from(startDate));
    return flareShortTermMonthForm;
  }

  public static FlareShortTermMonthForm from(FlareShortTermMonth flareShortTermMonth) {
    var flareShortTermMonthForm = new FlareShortTermMonthForm();
    flareShortTermMonthForm.setStartDate(flareShortTermMonth.getStartDate());
    flareShortTermMonthForm.setEndDate(flareShortTermMonth.getEndDate());
    flareShortTermMonthForm.setConsentDays(
        DateUtils.daysBetweenInclusive(flareShortTermMonth.getStartDate(), flareShortTermMonth.getEndDate())
    );
    flareShortTermMonthForm.updateFromFlareVentRow(flareShortTermMonth);
    return flareShortTermMonthForm;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public Integer getConsentDays() {
    return consentDays;
  }

  public void setConsentDays(Integer consentDays) {
    this.consentDays = consentDays;
  }

  public Pair<LocalDate, LocalDate> getMonthTerm() {
    return Pair.of(this.startDate, this.endDate);
  }
  
}
