package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

class FlareReportPeriodHelperServiceTest {

  private FlareReportPeriodHelperService flareReportPeriodHelperService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = FlareReportTestUtil.flareAppVersion;
    flareReportPeriodHelperService = new FlareReportPeriodHelperService();
  }

  @Test
  void getProposedReportEndYearMonth() {
    assertThat(flareReportPeriodHelperService.getProposedReportEndYearMonth(applicationVersion))
        .isEqualTo(YearMonth.now().minusMonths(1));
  }

  @Test
  void getProposedReportStartYearMonth() {
    assertThat(flareReportPeriodHelperService.getProposedReportStartYearMonth(applicationVersion))
        .isEqualTo(YearMonth.now().minusMonths(12));
  }

}
