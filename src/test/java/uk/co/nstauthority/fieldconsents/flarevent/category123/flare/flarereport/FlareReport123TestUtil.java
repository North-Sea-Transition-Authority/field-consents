package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class FlareReport123TestUtil {

  public static List<FlareReport123Month> getFlareReport123MonthsForYear(ApplicationVersion applicationVersion, int year) {
    List<FlareReport123Month> flareReport123Months = new ArrayList<>();

    FlareReport123Month flareReport123Month;
    for (Month month : Month.values()) {
      flareReport123Month = new FlareReport123Month();
      flareReport123Month.setApplicationVersion(applicationVersion);
      flareReport123Month.setYear(year);
      flareReport123Month.setMonth(month);
      flareReport123Month.setShutDownDays(month.getValue());
      flareReport123Month.setComments("comment" + month.getValue());
      flareReport123Month.setCategory1(BigDecimal.valueOf(month.getValue()));
      flareReport123Month.setCategory2(BigDecimal.valueOf(month.getValue() * 10));
      flareReport123Month.setCategory3(BigDecimal.valueOf(month.getValue() * 100));

      flareReport123Months.add(flareReport123Month);
    }
    return flareReport123Months;
  }

  public static List<FlareReport123Month> getFlareReport123MonthsForSplitYear(ApplicationVersion applicationVersion, int startYear) {
    List<FlareReport123Month> flareReport123Months = new ArrayList<>();

    FlareReport123Month flareReport123Month;
    for (Month month : Month.values()) {
      var year = month.getValue() < 6 ? startYear + 1 : startYear;
      flareReport123Month = new FlareReport123Month();
      flareReport123Month.setApplicationVersion(applicationVersion);
      flareReport123Month.setYear(year);
      flareReport123Month.setMonth(month);
      flareReport123Month.setShutDownDays(month.getValue());
      flareReport123Month.setComments("comment" + month.getValue());
      flareReport123Month.setCategory1(BigDecimal.valueOf(month.getValue()));
      flareReport123Month.setCategory2(BigDecimal.valueOf(month.getValue() * 10));
      flareReport123Month.setCategory3(BigDecimal.valueOf(month.getValue() * 100));

      flareReport123Months.add(flareReport123Month);
    }
    return flareReport123Months;
  }
}
