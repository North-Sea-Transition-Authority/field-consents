package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualMonth;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermMonth;

class EmissionShortTermUtilTest {

  @Test
  void getShortTermMonthConsentDays_withFlareShortTermMonth() {
    var flareShortTermMonth = new FlareShortTermMonth();

    var startDate = LocalDate.of(2022, Month.OCTOBER, 1);
    flareShortTermMonth.setStartDate(startDate);

    flareShortTermMonth.setEndDate(LocalDate.of(2022, Month.OCTOBER, 31));
    assertThat(EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth)).isEqualTo(31);

    flareShortTermMonth.setEndDate(LocalDate.of(2022, Month.OCTOBER, 10));
    assertThat(EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth)).isEqualTo(10);

    flareShortTermMonth.setEndDate(LocalDate.of(2022, Month.OCTOBER, 20));
    assertThat(EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth)).isEqualTo(20);
  }

  @Test
  void getShortTermMonthConsentDays_withFlareShortTermMonth_differentYearMonth() {
    var flareShortTermMonth = new FlareShortTermMonth();

    var startDate = LocalDate.of(2022, Month.OCTOBER, 1);
    flareShortTermMonth.setStartDate(startDate);

    var endDateInDifferentMonth = LocalDate.of(2022, Month.NOVEMBER, 20);
    flareShortTermMonth.setEndDate(endDateInDifferentMonth);
    assertThatThrownBy(() -> EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("The start date 2022-10-01 and end date 2022-11-20 should be in the same month and year");

    var endDateSameMonthDifferentYear = LocalDate.of(2023, Month.OCTOBER, 20);
    flareShortTermMonth.setEndDate(endDateSameMonthDifferentYear);
    assertThatThrownBy(() -> EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("The start date 2022-10-01 and end date 2023-10-20 should be in the same month and year");
  }

  @Test
  void getShortTermMonthConsentDays_withVentShortTermMonth() {
    var flareShortTermMonth = new VentShortTermMonth();

    var startDate = LocalDate.of(2022, Month.OCTOBER, 1);
    flareShortTermMonth.setStartDate(startDate);

    flareShortTermMonth.setEndDate(LocalDate.of(2022, Month.OCTOBER, 31));
    assertThat(EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth)).isEqualTo(31);

    flareShortTermMonth.setEndDate(LocalDate.of(2022, Month.OCTOBER, 10));
    assertThat(EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth)).isEqualTo(10);

    flareShortTermMonth.setEndDate(LocalDate.of(2022, Month.OCTOBER, 20));
    assertThat(EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth)).isEqualTo(20);
  }

  @Test
  void getShortTermMonthConsentDays_withVentShortTermMonth_differentYearMonth() {
    var flareShortTermMonth = new VentShortTermMonth();

    var startDate = LocalDate.of(2022, Month.OCTOBER, 1);
    flareShortTermMonth.setStartDate(startDate);

    var endDateInDifferentMonth = LocalDate.of(2022, Month.NOVEMBER, 20);
    flareShortTermMonth.setEndDate(endDateInDifferentMonth);
    assertThatThrownBy(() -> EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("The start date 2022-10-01 and end date 2022-11-20 should be in the same month and year");

    var endDateSameMonthDifferentYear = LocalDate.of(2023, Month.OCTOBER, 20);
    flareShortTermMonth.setEndDate(endDateSameMonthDifferentYear);
    assertThatThrownBy(() -> EmissionShortTermUtil.getShortTermMonthConsentDays(flareShortTermMonth))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("The start date 2022-10-01 and end date 2023-10-20 should be in the same month and year");
  }

  @Test
  void getShortTermMonthConsentDays_withFlareAnnualMonth() {
    var flareAnnualMonth = new FlareAnnualMonth();

    assertThatThrownBy(() -> EmissionShortTermUtil.getShortTermMonthConsentDays(flareAnnualMonth))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unexpected consent month class: uk.co.nstauthority.fieldconsents.flarevent.flare.annual.FlareAnnualMonth");
  }
}
