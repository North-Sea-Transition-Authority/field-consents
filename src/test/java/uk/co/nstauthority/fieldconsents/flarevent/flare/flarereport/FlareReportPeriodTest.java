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
  static final YearMonth proposedYearMonth = YearMonth.of(2022, Month.OCTOBER);
  FlareReportPeriodForm flareReportPeriodForm;


  @BeforeEach
  void setUp() {
    flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.setHasDataForPeriod(Boolean.TRUE);
    flareReportPeriodForm.getReportEndMonth().setInputValue("January");
    flareReportPeriodForm.getReportEndYear().setInputValue("2021");
  }

  @Test
  void newFrom_hasHasDataForPeriodTrue() {
    FlareReportPeriod flareReportPeriod =
        FlareReportPeriod.from(applicationVersion, flareReportPeriodForm, proposedYearMonth);

    assertThat(flareReportPeriod)
        .extracting(
            FlareReportPeriod::getApplicationVersion,
            FlareReportPeriod::getHasDataForPeriod,
            FlareReportPeriod::getReportEndMonth,
            FlareReportPeriod::getReportEndYear,
            FlareReportPeriod::getReportStartYearMonth,
            FlareReportPeriod::getReportEndYearMonth
        )
        .containsExactly(
            applicationVersion,
            Boolean.TRUE,
            Month.OCTOBER,
            2022,
            YearMonth.of(2022, Month.OCTOBER).minusMonths(11),
            YearMonth.of(2022, Month.OCTOBER)
        );
  }

  @Test
  void newFrom_hasHasDataForPeriodFalseBadMonth() {
    flareReportPeriodForm.setHasDataForPeriod(Boolean.FALSE);
    flareReportPeriodForm.getReportEndMonth().setInputValue("NoAMonth");

    assertThatThrownBy(() -> FlareReportPeriod.from(applicationVersion, flareReportPeriodForm, proposedYearMonth))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No enum constant java.time.Month.NOAMONTH");
  }

  @Test
  void newFrom_hasHasDataForPeriodFalseBadYear() {
    flareReportPeriodForm.setHasDataForPeriod(Boolean.FALSE);
    flareReportPeriodForm.getReportEndYear().setInputValue("NoAYear");

    assertThatThrownBy(() -> FlareReportPeriod.from(applicationVersion, flareReportPeriodForm, proposedYearMonth))
        .isInstanceOf(NoSuchElementException.class);
  }

}
