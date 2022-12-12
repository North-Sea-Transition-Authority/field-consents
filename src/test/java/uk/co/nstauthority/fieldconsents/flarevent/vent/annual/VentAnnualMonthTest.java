package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class VentAnnualMonthTest {

  @Test
  void newFromVentAnnualMonthForm() {
    ApplicationVersion applicationVersion = VentAnnualTestUtil.ventAppVersion;

    VentAnnualMonth ventAnnualMonth = VentAnnualMonth.from(applicationVersion,
        VentAnnualTestUtil.getFullVentAnnualMonthForm());

    assertThat(ventAnnualMonth)
        .extracting(
            VentAnnualMonth::getApplicationVersion,
            VentAnnualMonth::getYear,
            VentAnnualMonth::getMonth,
            VentAnnualMonth::getComments,
            VentAnnualMonth::getCategoryA,
            VentAnnualMonth::getCategoryB,
            VentAnnualMonth::getCategoryC)
        .containsExactly(
            applicationVersion,
            2024, Month.APRIL, "Test form comments.",
            BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)
        );
  }

}
