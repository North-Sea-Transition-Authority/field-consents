package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class VentAnnual123TestUtil {

  public static List<VentAnnual123Month> getVentAnnual123MonthsForYear(ApplicationVersion applicationVersion, int year) {
    List<VentAnnual123Month> ventAnnual123Months = new ArrayList<>();

    VentAnnual123Month ventAnnual123Month;
    for (Month month : Month.values()) {
      ventAnnual123Month = new VentAnnual123Month();
      ventAnnual123Month.setApplicationVersion(applicationVersion);
      ventAnnual123Month.setYear(year);
      ventAnnual123Month.setMonth(month);
      ventAnnual123Month.setComments("comment" + month.getValue());
      ventAnnual123Month.setCategory1(BigDecimal.valueOf(month.getValue()));

      ventAnnual123Months.add(ventAnnual123Month);
    }
    return ventAnnual123Months;
  }
}
