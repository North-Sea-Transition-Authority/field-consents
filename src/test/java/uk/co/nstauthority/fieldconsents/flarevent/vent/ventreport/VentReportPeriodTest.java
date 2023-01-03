package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

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

class VentReportPeriodTest {

  static final ApplicationVersion applicationVersion =
      ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);

  FlareVentReportPeriodForm reportPeriodForm;


  @BeforeEach
  void setUp() {
    reportPeriodForm = new FlareVentReportPeriodForm();
    reportPeriodForm.getReportEndMonth().setInputValue("January");
    reportPeriodForm.getReportEndYear().setInputValue("2021");
  }

  @Test
  void newFrom_ventReportPeriodForm() {
    VentReportPeriod ventReportPeriod =
        VentReportPeriod.from(applicationVersion, reportPeriodForm);

    assertThat(ventReportPeriod)
        .extracting(
            VentReportPeriod::getApplicationVersion,
            VentReportPeriod::getReportEndMonth,
            VentReportPeriod::getReportEndYear,
            VentReportPeriod::getReportStartYearMonth,
            VentReportPeriod::getReportEndYearMonth
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

    assertThatThrownBy(() -> VentReportPeriod.from(applicationVersion, reportPeriodForm))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No enum constant java.time.Month.NOAMONTH");
  }

  @Test
  void newFrom_badYear() {
    reportPeriodForm.getReportEndYear().setInputValue("NoAYear");

    assertThatThrownBy(() -> VentReportPeriod.from(applicationVersion, reportPeriodForm))
        .isInstanceOf(NoSuchElementException.class);
  }

}
