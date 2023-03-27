package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Month;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class FlareAnnualMonthFormTest {

  @Test
  void newFromYearMonth() {
    YearMonth yearMonth = YearMonth.of(2022, Month.JANUARY);
    var flareAnnualMonthForm = FlareAnnualMonthForm.from(yearMonth);

    assertThat(flareAnnualMonthForm)
        .extracting(
            FlareAnnualMonthForm::getYear,
            FlareAnnualMonthForm::getMonth,
            form -> form.getComments().getDisplayName(),
            form -> form.getComments().getFieldName(),
            form -> form.getComments().getInputValue()
        )
        .containsExactly(
            "2022", "January",
            "comments", "comments", null
        );
  }

  @Test
  void newFromFlareAnnualForm() {
    var flareAnnualMonth = FlareAnnualTestUtil.getFullFlareAnnualMonth();
    var flareAnnualMonthForm = FlareAnnualMonthForm.from(flareAnnualMonth);

    assertThat(flareAnnualMonthForm)
        .extracting(
            FlareAnnualMonthForm::getYear,
            FlareAnnualMonthForm::getMonth,
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
            "comments", "comments", "Test comments.",
            "Category A", "categoryA", "0",
            "Category B", "categoryB", "1",
            "Category C", "categoryC", "999.999"
        );
  }

}
