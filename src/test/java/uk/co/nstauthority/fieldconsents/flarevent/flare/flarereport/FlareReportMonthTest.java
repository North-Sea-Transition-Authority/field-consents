package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class FlareReportMonthTest {

  @Test
  void newFromFlareReportMonthForm() {
    ApplicationVersion applicationVersion =
        ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);

    FlareReportMonth flareReportMonth = FlareReportMonth.from(applicationVersion,
        FlareReportTestUtil.getFullFlareReportMonthForm());

    assertThat(flareReportMonth)
        .extracting(
            FlareReportMonth::getApplicationVersion,
            FlareReportMonth::getYear,
            FlareReportMonth::getMonth,
            FlareReportMonth::getShutDownDays,
            FlareReportMonth::getComments,
            FlareReportMonth::getCategoryA,
            FlareReportMonth::getCategoryB,
            FlareReportMonth::getCategoryC
        )
        .containsExactly(
            applicationVersion,
            2024, Month.APRIL, 10, "Test form comments.",
            BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)
        );
  }

}
