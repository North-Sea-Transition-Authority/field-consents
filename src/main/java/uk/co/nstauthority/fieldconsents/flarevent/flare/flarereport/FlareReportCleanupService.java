package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.time.YearMonth;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.ReportUtil;

@Service
class FlareReportCleanupService {

  private final FlareReportMonthRepository flareReportMonthRepository;

  @Autowired
  FlareReportCleanupService(FlareReportMonthRepository flareReportMonthRepository) {
    this.flareReportMonthRepository = flareReportMonthRepository;
  }

  @Transactional
  public void removeObsoleteReportDataOnPeriodSave(ApplicationVersion applicationVersion, FlareReportPeriod flareReportPeriod) {
    Set<YearMonth> expectedYearMonthsSet = ReportUtil.getSetOfExpectedYearMonthsForPeriod(
        flareReportPeriod.getReportStartYearMonth(),
        flareReportPeriod.getReportEndYearMonth()
    );

    // loop over the existing saved flare report months and delete any that are obsolete
    for (FlareReportMonth flareReportMonth: flareReportMonthRepository.findAllByApplicationVersion(applicationVersion)) {
      if (!expectedYearMonthsSet.contains(YearMonth.of(flareReportMonth.getYear(), flareReportMonth.getMonth()))) {
        flareReportMonthRepository.delete(flareReportMonth);
      }
    }
  }
}
