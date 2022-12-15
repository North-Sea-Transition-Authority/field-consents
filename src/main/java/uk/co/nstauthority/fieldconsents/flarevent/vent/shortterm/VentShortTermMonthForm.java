package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import java.time.LocalDate;
import java.time.YearMonth;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowForm;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class VentShortTermMonthForm extends FlareVentRowForm {

  private LocalDate startDate;

  private LocalDate endDate;

  private Integer consentDays;

  public static VentShortTermMonthForm from(LocalDate startDate, LocalDate endDate) {
    var ventShortTermMonthForm = new VentShortTermMonthForm();
    ventShortTermMonthForm.setStartDate(startDate);
    ventShortTermMonthForm.setEndDate(endDate);
    ventShortTermMonthForm.setConsentDays(DateUtils.daysBetweenInclusive(startDate, endDate));
    ventShortTermMonthForm.updateFromYearMonth(YearMonth.from(startDate));
    return ventShortTermMonthForm;
  }

  public static VentShortTermMonthForm from(VentShortTermMonth ventShortTermMonth) {
    var ventShortTermMonthForm = new VentShortTermMonthForm();
    ventShortTermMonthForm.setStartDate(ventShortTermMonth.getStartDate());
    ventShortTermMonthForm.setEndDate(ventShortTermMonth.getEndDate());
    ventShortTermMonthForm.setConsentDays(
        DateUtils.daysBetweenInclusive(ventShortTermMonth.getStartDate(), ventShortTermMonth.getEndDate())
    );
    ventShortTermMonthForm.updateFromFlareVentRow(ventShortTermMonth);
    return ventShortTermMonthForm;
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
