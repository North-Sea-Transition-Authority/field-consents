package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Month;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class VentAnnualMonthFormTest {

  @Test
  void newFromYearMonth() {
    YearMonth yearMonth = YearMonth.of(2022, Month.JANUARY);
    var ventAnnualMonthForm = VentAnnualMonthForm.from(yearMonth);

    assertThat(ventAnnualMonthForm)
        .extracting(
            VentAnnualMonthForm::getYear,
            VentAnnualMonthForm::getMonth,
            form -> form.getComments().getDisplayName(),
            form -> form.getComments().getFieldName(),
            form -> form.getComments().getInputValue()
        )
        .containsExactly(
            "2022", "January",
            "Comments", "comments", null
        );
  }

  @Test
  void newFromVentAnnualForm() {
    var ventAnnualMonth = VentAnnualTestUtil.getFullVentAnnualMonth();
    var ventAnnualMonthForm = VentAnnualMonthForm.from(ventAnnualMonth);

    assertThat(ventAnnualMonthForm)
        .extracting(
            VentAnnualMonthForm::getYear,
            VentAnnualMonthForm::getMonth,
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
            "Comments", "comments", "Test comments.",
            "Category A", "categoryA", "0",
            "Category B", "categoryB", "1",
            "Category C", "categoryC", "999.999"
        );
  }

}
