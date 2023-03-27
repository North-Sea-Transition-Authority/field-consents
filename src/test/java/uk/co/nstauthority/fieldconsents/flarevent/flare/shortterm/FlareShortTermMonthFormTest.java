package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

class FlareShortTermMonthFormTest {

  @Test
  void newFromYearMonth() {
    LocalDate startDate = LocalDate.of(2022, Month.JANUARY, 11);
    LocalDate endDate = LocalDate.of(2022, Month.JANUARY, 25);
    var flareShortTermMonthForm = FlareShortTermMonthForm.from(startDate, endDate);

    assertThat(flareShortTermMonthForm)
        .extracting(
            FlareShortTermMonthForm::getYear,
            FlareShortTermMonthForm::getMonth,
            FlareShortTermMonthForm::getStartDate,
            FlareShortTermMonthForm::getEndDate,
            FlareShortTermMonthForm::getConsentDays,
            form -> form.getCategoryA().getDisplayName(),
            form -> form.getCategoryA().getFieldName(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getDisplayName(),
            form -> form.getCategoryB().getFieldName(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getDisplayName(),
            form -> form.getCategoryC().getFieldName(),
            form -> form.getCategoryC().getInputValue(),
            form -> form.getComments().getDisplayName(),
            form -> form.getComments().getFieldName(),
            form -> form.getComments().getInputValue()
        )
        .containsExactly(
            "2022", "January",
            startDate, endDate, 15,
            "Category A", "categoryA", null,
            "Category B", "categoryB", null,
            "Category C", "categoryC", null,
            "comments", "comments", null
        );
  }

  @Test
  void newFromFlareShortTermForm() {
    var flareShortTermMonth = FlareShortTermTestUtil.getFullFlareShortTermMonth();
    var flareShortTermMonthForm = FlareShortTermMonthForm.from(flareShortTermMonth);

    assertThat(flareShortTermMonthForm)
        .extracting(
            FlareShortTermMonthForm::getYear,
            FlareShortTermMonthForm::getMonth,
            FlareShortTermMonthForm::getStartDate,
            FlareShortTermMonthForm::getEndDate,
            FlareShortTermMonthForm::getConsentDays,
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
            LocalDate.of(2023, Month.MARCH, 1),
            LocalDate.of(2023, Month.MARCH, 15),
            15,
            "comments", "comments", "Test comments.",
            "Category A", "categoryA", "0",
            "Category B", "categoryB", "1",
            "Category C", "categoryC", "999.999"
        );
  }

}
