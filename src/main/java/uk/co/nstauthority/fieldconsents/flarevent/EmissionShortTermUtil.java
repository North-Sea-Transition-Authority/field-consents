package uk.co.nstauthority.fieldconsents.flarevent;

import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm.FlareShortTermMonth;
import uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm.VentShortTermMonth;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class EmissionShortTermUtil {

  private EmissionShortTermUtil() {
    throw new IllegalStateException("EmissionShortTermUtil is a utility class and cannot be instantiated");
  }

  public static int getShortTermMonthConsentDays(FlareVentRow shortTermConsentMonth) {
    LocalDate monthStartDate;
    LocalDate monthEndDate;
    if (shortTermConsentMonth instanceof FlareShortTermMonth flareShortTermConsentMonth) {
      monthStartDate = flareShortTermConsentMonth.getStartDate();
      monthEndDate = flareShortTermConsentMonth.getEndDate();
    } else if (shortTermConsentMonth instanceof VentShortTermMonth ventShortTermConsentMonth) {
      monthStartDate = ventShortTermConsentMonth.getStartDate();
      monthEndDate = ventShortTermConsentMonth.getEndDate();
    } else {
      throw new RuntimeException("Unexpected consent month class: " + shortTermConsentMonth.getClass().getName());
    }

    return DateUtils.daysBetweenInclusive(monthStartDate, monthEndDate);
  }
}
