package uk.co.nstauthority.fieldconsents.fee;

import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class FeePeriodUtil {

  private FeePeriodUtil() {
    throw new IllegalStateException("FeePeriodUtil is a utility class and cannot be instantiated");
  }

  public static String getTitle(FeePeriodDto feePeriodDto) {
    var title = DateUtils.format(feePeriodDto.startDate().atStartOfDay(), DateUtils.SHORT_DATE);
    if (feePeriodDto.endDate() == null) {
      title += " and onwards";
    } else {
      title += " - " + DateUtils.format(DateUtils.atEndOfDay(feePeriodDto.endDate()), DateUtils.SHORT_DATE);
    }
    return title;
  }
}
