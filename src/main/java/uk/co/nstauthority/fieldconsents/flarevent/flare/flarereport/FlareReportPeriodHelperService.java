package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
class FlareReportPeriodHelperService {

  YearMonth getProposedReportEndYearMonth(ApplicationVersion applicationVersion) {
    LocalDate applicationDate = applicationVersion.getApplication().getCreatedLocalDate();
    return YearMonth.of(applicationDate.getYear(), applicationDate.getMonth().minus(1));
  }

  YearMonth getProposedReportStartYearMonth(ApplicationVersion applicationVersion) {
    return getProposedReportEndYearMonth(applicationVersion).minusMonths(11);
  }

}
