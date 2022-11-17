package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Month;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class FlareReportMonthFormTest {

  @Test
  void newFromYearMonth() {
    YearMonth yearMonth = YearMonth.of(2022, Month.JANUARY);
    var flareReportMonthForm = FlareReportMonthForm.from(yearMonth);

    assertThat(flareReportMonthForm)
        .extracting(
            FlareReportMonthForm::getYear,
            FlareReportMonthForm::getMonth,
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
  void newFromFlareReportForm() {
    var flareReportMonth = FlareReportTestUtil.getFullFlareReportMonth();
    var flareReportMonthForm = FlareReportMonthForm.from(flareReportMonth);

    assertThat(flareReportMonthForm)
        .extracting(
            FlareReportMonthForm::getYear,
            FlareReportMonthForm::getMonth,
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
