package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class FlareAnnualMonthTest {

  @Test
  void newFromFlareAnnualMonthForm() {
    ApplicationVersion applicationVersion = FlareAnnualTestUtil.flareAppVersion;

    FlareAnnualMonth flareAnnualMonth = FlareAnnualMonth.from(applicationVersion,
        FlareAnnualTestUtil.getFullFlareAnnualMonthForm());

    assertThat(flareAnnualMonth)
        .extracting(
            FlareAnnualMonth::getApplicationVersion,
            FlareAnnualMonth::getYear,
            FlareAnnualMonth::getMonth,
            FlareAnnualMonth::getComments,
            FlareAnnualMonth::getCategoryA,
            FlareAnnualMonth::getCategoryB,
            FlareAnnualMonth::getCategoryC)
        .containsExactly(
            applicationVersion,
            2024, Month.APRIL, "Test form comments.",
            BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)
        );
  }

}
