package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Month;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class VentReportMonthFormTest {

  @Test
  void newFromYearMonth() {
    YearMonth yearMonth = YearMonth.of(2022, Month.JANUARY);
    var ventReportMonthForm = VentReportMonthForm.from(yearMonth);

    assertThat(ventReportMonthForm)
        .extracting(
            VentReportMonthForm::getYear,
            VentReportMonthForm::getMonth,
            form -> form.getShutDownDays().getDisplayName(),
            form -> form.getShutDownDays().getFieldName(),
            form -> form.getShutDownDays().getInputValue(),
            form -> form.getComments().getDisplayName(),
            form -> form.getComments().getFieldName(),
            form -> form.getComments().getInputValue()
        )
        .containsExactly(
            "2022", "January",
            "Days of total shutdown", "shutDownDays", null,
            "Comments", "comments", null
        );
  }

  @Test
  void newFromVentReportForm() {
    var ventReportMonth = VentReportTestUtil.getFullVentReportMonth();
    var ventReportMonthForm = VentReportMonthForm.from(ventReportMonth);

    assertThat(ventReportMonthForm)
        .extracting(
            VentReportMonthForm::getYear,
            VentReportMonthForm::getMonth,
            form -> form.getShutDownDays().getDisplayName(),
            form -> form.getShutDownDays().getFieldName(),
            form -> form.getShutDownDays().getInputValue(),
            form -> form.getComments().getDisplayName(),
            form -> form.getComments().getFieldName(),
            form -> form.getComments().getInputValue(),
            form -> form.getCategoryA().getDisplayName(),
            form -> form.getCategoryA().getFieldName(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getDisplayName(),
            form -> form.getCategoryB().getFieldName(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getDisplayName(),
            form -> form.getCategoryC().getFieldName(),
            form -> form.getCategoryC().getInputValue()
        )
        .containsExactly(
            "2023", "March",
            "Days of total shutdown", "shutDownDays", "15",
            "Comments", "comments", "Test comments.",
            "Category A", "categoryA", "0",
            "Category B", "categoryB", "1",
            "Category C", "categoryC", "999.999"
        );
  }
}
