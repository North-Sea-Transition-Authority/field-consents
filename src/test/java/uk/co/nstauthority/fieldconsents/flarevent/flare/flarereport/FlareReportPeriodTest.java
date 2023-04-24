package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Month;
import java.time.YearMonth;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;

class FlareReportPeriodTest {

  static final ApplicationVersion applicationVersion =
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

  FlareVentReportPeriodForm reportPeriodForm;


  @BeforeEach
  void setUp() {
    reportPeriodForm = new FlareVentReportPeriodForm();
    reportPeriodForm.getReportEndMonth().setInputValue("January");
    reportPeriodForm.getReportEndYear().setInputValue("2021");
  }

  @Test
  void newFrom_flareReportPeriodForm() {
    FlareReportPeriod flareReportPeriod =
        FlareReportPeriod.from(applicationVersion, reportPeriodForm);

    assertThat(flareReportPeriod)
        .extracting(
            FlareReportPeriod::getApplicationVersion,
            FlareReportPeriod::getReportEndMonth,
            FlareReportPeriod::getReportEndYear,
            FlareReportPeriod::getReportStartYearMonth,
            FlareReportPeriod::getReportEndYearMonth
        )
        .containsExactly(
            applicationVersion,
            Month.JANUARY,
            2021,
            YearMonth.of(2021, Month.JANUARY).minusMonths(11),
            YearMonth.of(2021, Month.JANUARY)
        );
  }

  @Test
  void newFrom_badMonth() {
    reportPeriodForm.getReportEndMonth().setInputValue("NoAMonth");

    assertThatThrownBy(() -> FlareReportPeriod.from(applicationVersion, reportPeriodForm))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No enum constant java.time.Month.NOAMONTH");
  }

  @Test
  void newFrom_badYear() {
    reportPeriodForm.getReportEndYear().setInputValue("NoAYear");

    assertThatThrownBy(() -> FlareReportPeriod.from(applicationVersion, reportPeriodForm))
        .isInstanceOf(NoSuchElementException.class);
  }

}
