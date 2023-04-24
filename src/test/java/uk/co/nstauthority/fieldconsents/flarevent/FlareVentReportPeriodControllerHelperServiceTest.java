package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.Year;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class FlareVentReportPeriodControllerHelperServiceTest {

  private FlareVentReportPeriodControllerHelperService reportPeriodControllerHelperService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(
        ApplicationType.FLARE);
    reportPeriodControllerHelperService = new FlareVentReportPeriodControllerHelperService();
  }

  @Test
  void getReportEndYearsMap() {
    Integer currentYear = Year.now().getValue();
    Map<String, String> reportEndYearsMap =
        reportPeriodControllerHelperService.getReportEndYearsMap(applicationVersion);

    assertThat(reportEndYearsMap)
        .containsExactly(
            entry(String.valueOf(currentYear - 1), String.valueOf(currentYear - 1)),
            entry(String.valueOf(currentYear), String.valueOf(currentYear))
        );
  }

}
