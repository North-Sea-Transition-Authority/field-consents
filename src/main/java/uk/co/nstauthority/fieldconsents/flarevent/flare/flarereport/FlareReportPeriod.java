package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Month;
import java.util.NoSuchElementException;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;

@Entity
@Table(name = "flare_report_periods")
public class FlareReportPeriod extends FlareVentReportPeriod {

  public FlareReportPeriod() {
  }

  @VisibleForTesting
  public FlareReportPeriod(ApplicationVersion applicationVersion,
                           Month reportEndMonth,
                           Integer reportEndYear) {
    super(applicationVersion, reportEndMonth, reportEndYear);
  }

  public static FlareReportPeriod from(ApplicationVersion applicationVersion,
                                       FlareVentReportPeriodForm reportPeriodForm) {

    FlareReportPeriod flareReportPeriod = new FlareReportPeriod();
    flareReportPeriod.setApplicationVersion(applicationVersion);
    flareReportPeriod.setReportEndMonth(
        Month.valueOf(reportPeriodForm.getReportEndMonth().getInputValue().toUpperCase()));
    flareReportPeriod.setReportEndYear(reportPeriodForm.getReportEndYear().getAsInteger()
        .orElseThrow(NoSuchElementException::new));

    return flareReportPeriod;
  }
}
