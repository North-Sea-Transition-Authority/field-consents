package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class FlareShortTermMonthTest {

  @Test
  void newFromFlareShortTermMonthForm() {
    ApplicationVersion applicationVersion = FlareShortTermTestUtil.flareAppVersion;

    FlareShortTermMonth flareShortTermMonth = FlareShortTermMonth.from(applicationVersion,
        FlareShortTermTestUtil.getFullFlareShortTermMonthForm());

    assertThat(flareShortTermMonth)
        .extracting(
            FlareShortTermMonth::getApplicationVersion,
            FlareShortTermMonth::getYear,
            FlareShortTermMonth::getMonth,
            FlareShortTermMonth::getStartDate,
            FlareShortTermMonth::getEndDate,
            FlareShortTermMonth::getComments,
            FlareShortTermMonth::getCategoryA,
            FlareShortTermMonth::getCategoryB,
            FlareShortTermMonth::getCategoryC)
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
