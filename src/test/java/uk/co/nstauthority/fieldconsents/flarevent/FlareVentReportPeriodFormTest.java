package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Month;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport.FlareReportPeriod;

class FlareVentReportPeriodFormTest {

  @Test
  void newFromFlareReportPeriod() {
    ApplicationVersion applicationVersion
        = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    FlareReportPeriod flareReportPeriod =
        new FlareReportPeriod(applicationVersion, Month.APRIL, 2023);

    FlareVentReportPeriodForm reportPeriodForm = FlareVentReportPeriodForm.from(flareReportPeriod);

    assertThat(reportPeriodForm)
        .extracting(
            form -> form.getReportEndMonth().getDisplayName(),
            form -> form.getReportEndMonth().getFieldName(),
            form -> form.getReportEndMonth().getInputValue(),
            form -> form.getReportEndYear().getDisplayName(),
            form -> form.getReportEndYear().getFieldName(),
            form -> form.getReportEndYear().getInputValue())
        .containsExactly(
            "Month", "reportEndMonth", "APRIL",
            "Year", "reportEndYear", "2023"
        );
  }

}
