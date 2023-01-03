package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import com.google.common.annotations.VisibleForTesting;
import java.time.Month;
import java.util.NoSuchElementException;
import javax.persistence.Entity;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriodForm;

@Entity
@Table(name = "vent_report_periods")
public class VentReportPeriod extends FlareVentReportPeriod {

  public VentReportPeriod() {
  }

  @VisibleForTesting
  public VentReportPeriod(ApplicationVersion applicationVersion,
                           Month reportEndMonth,
                           Integer reportEndYear) {
    super(applicationVersion, reportEndMonth, reportEndYear);
  }

  public static VentReportPeriod from(ApplicationVersion applicationVersion,
                                      FlareVentReportPeriodForm reportPeriodForm) {

    VentReportPeriod ventReportPeriod = new VentReportPeriod();
    ventReportPeriod.setApplicationVersion(applicationVersion);
    ventReportPeriod.setReportEndMonth(
        Month.valueOf(reportPeriodForm.getReportEndMonth().getInputValue().toUpperCase()));
    ventReportPeriod.setReportEndYear(reportPeriodForm.getReportEndYear().getAsInteger()
        .orElseThrow(NoSuchElementException::new));

    return ventReportPeriod;
  }
}
