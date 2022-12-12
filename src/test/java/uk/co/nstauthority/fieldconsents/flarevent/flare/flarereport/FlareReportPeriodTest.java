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

class FlareReportPeriodTest {

  static final ApplicationVersion applicationVersion =
      ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);

  FlareReportPeriodForm flareReportPeriodForm;


  @BeforeEach
  void setUp() {
    flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.getReportEndMonth().setInputValue("January");
    flareReportPeriodForm.getReportEndYear().setInputValue("2021");
  }

  @Test
  void newFrom_flareReportPeriodForm() {
    FlareReportPeriod flareReportPeriod =
        FlareReportPeriod.from(applicationVersion, flareReportPeriodForm);

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
    flareReportPeriodForm.getReportEndMonth().setInputValue("NoAMonth");

    assertThatThrownBy(() -> FlareReportPeriod.from(applicationVersion, flareReportPeriodForm))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No enum constant java.time.Month.NOAMONTH");
  }

  @Test
  void newFrom_badYear() {
    flareReportPeriodForm.getReportEndYear().setInputValue("NoAYear");

    assertThatThrownBy(() -> FlareReportPeriod.from(applicationVersion, flareReportPeriodForm))
        .isInstanceOf(NoSuchElementException.class);
  }

}
