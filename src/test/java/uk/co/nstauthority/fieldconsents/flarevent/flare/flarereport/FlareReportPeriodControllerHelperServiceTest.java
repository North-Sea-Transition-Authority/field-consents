package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.Year;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class FlareReportPeriodControllerHelperServiceTest {

  private FlareReportPeriodControllerHelperService flareReportPeriodControllerHelperService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = FlareReportTestUtil.flareAppVersion;
    flareReportPeriodControllerHelperService = new FlareReportPeriodControllerHelperService();
  }

  @Test
  void getReportEndYearsMap() {
    Integer currentYear = Year.now().getValue();
    Map<String, String> reportEndYearsMap =
        flareReportPeriodControllerHelperService.getReportEndYearsMap(applicationVersion);

    assertThat(reportEndYearsMap)
        .containsExactly(
            entry(String.valueOf(currentYear - 1), String.valueOf(currentYear - 1)),
            entry(String.valueOf(currentYear), String.valueOf(currentYear))
        );
  }

}
