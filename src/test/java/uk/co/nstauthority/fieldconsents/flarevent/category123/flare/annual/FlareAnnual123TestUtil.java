package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.annual;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class FlareAnnual123TestUtil {

  public static List<FlareAnnual123Month> getFlareAnnual123MonthsForYear(ApplicationVersion applicationVersion, int year) {
    List<FlareAnnual123Month> flareAnnual123Months = new ArrayList<>();

    FlareAnnual123Month flareAnnual123Month;
    for (Month month : Month.values()) {
      flareAnnual123Month = new FlareAnnual123Month();
      flareAnnual123Month.setApplicationVersion(applicationVersion);
      flareAnnual123Month.setYear(year);
      flareAnnual123Month.setMonth(month);
      flareAnnual123Month.setComments("comment" + month.getValue());
      flareAnnual123Month.setCategory1(BigDecimal.valueOf(month.getValue()));
      flareAnnual123Month.setCategory2(BigDecimal.valueOf(month.getValue() * 10));
      flareAnnual123Month.setCategory3(BigDecimal.valueOf(month.getValue() * 100));

      flareAnnual123Months.add(flareAnnual123Month);
    }
    return flareAnnual123Months;
  }
}
