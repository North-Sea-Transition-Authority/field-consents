package uk.co.nstauthority.fieldconsents.charts;

import java.time.YearMonth;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public enum EmissionsChartType {

  REPORT,
  CONSENT;

  public String getHeading(ApplicationType applicationType, YearMonth start, YearMonth end) {
    return "%s %s (%s - %s)".formatted(
        applicationType.getDisplayName(),
        this.name().toLowerCase(),
        DateUtils.formatShort(start),
        DateUtils.formatShort(end)
    );
  }

}
