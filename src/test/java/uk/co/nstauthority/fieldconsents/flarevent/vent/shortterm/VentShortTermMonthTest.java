package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class VentShortTermMonthTest {

  @Test
  void newFromVentShortTermMonthForm() {
    ApplicationVersion applicationVersion = VentShortTermTestUtil.ventAppVersion;

    VentShortTermMonth ventShortTermMonth = VentShortTermMonth.from(applicationVersion,
        VentShortTermTestUtil.getFullVentShortTermMonthForm());

    assertThat(ventShortTermMonth)
        .extracting(
            VentShortTermMonth::getApplicationVersion,
            VentShortTermMonth::getYear,
            VentShortTermMonth::getMonth,
            VentShortTermMonth::getStartDate,
            VentShortTermMonth::getEndDate,
            VentShortTermMonth::getComments,
            VentShortTermMonth::getCategoryA,
            VentShortTermMonth::getCategoryB,
            VentShortTermMonth::getCategoryC)
        .containsExactly(
            applicationVersion,
            2024, Month.APRIL,
            LocalDate.of(2024, Month.APRIL, 11),
            LocalDate.of(2024, Month.APRIL, 20),
            "Test form comments.",
            BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)
        );
  }

}
