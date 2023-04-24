package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class VentReportMonthTest {

  @Test
  void newFromVentReportMonthForm() {
    ApplicationVersion applicationVersion =
        ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    VentReportMonth ventReportMonth = VentReportMonth.from(applicationVersion,
        VentReportTestUtil.getFullVentReportMonthForm());

    assertThat(ventReportMonth)
        .extracting(
            VentReportMonth::getApplicationVersion,
            VentReportMonth::getYear,
            VentReportMonth::getMonth,
            VentReportMonth::getShutDownDays,
            VentReportMonth::getComments,
            VentReportMonth::getCategoryA,
            VentReportMonth::getCategoryB,
            VentReportMonth::getCategoryC
        )
        .containsExactly(
            applicationVersion,
            2024, Month.APRIL, 10, "Test form comments.",
            BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.valueOf(3)
        );
  }

}
