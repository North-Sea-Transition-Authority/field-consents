package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.time.YearMonth;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportPeriod;
import uk.co.nstauthority.fieldconsents.flarevent.ReportUtil;

@Service
class VentReportCleanupService {

  private final VentReportMonthRepository ventReportMonthRepository;

  @Autowired
  VentReportCleanupService(VentReportMonthRepository ventReportMonthRepository) {
    this.ventReportMonthRepository = ventReportMonthRepository;
  }

  @Transactional
  public void removeObsoleteReportDataOnPeriodSave(
      ApplicationVersion applicationVersion,
      FlareVentReportPeriod reportPeriod) {
    Set<YearMonth> expectedYearMonthsSet = ReportUtil.getSetOfExpectedYearMonthsForPeriod(
        reportPeriod.getReportStartYearMonth(),
        reportPeriod.getReportEndYearMonth()
    );

    // loop over the existing saved vent report months and delete any that are obsolete
    for (VentReportMonth ventReportMonth: ventReportMonthRepository.findAllByApplicationVersion(applicationVersion)) {
      if (!expectedYearMonthsSet.contains(YearMonth.of(ventReportMonth.getYear(), ventReportMonth.getMonth()))) {
        ventReportMonthRepository.delete(ventReportMonth);
      }
    }
  }
}
