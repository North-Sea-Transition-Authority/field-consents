package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class VentReport123TestUtil {

  public static List<VentReport123Month> getVentReport123MonthsForYear(ApplicationVersion applicationVersion, int year) {
    List<VentReport123Month> ventReport123Months = new ArrayList<>();

    VentReport123Month ventReport123Month;
    for (Month month : Month.values()) {
      ventReport123Month = new VentReport123Month();
      ventReport123Month.setApplicationVersion(applicationVersion);
      ventReport123Month.setYear(year);
      ventReport123Month.setMonth(month);
      ventReport123Month.setShutDownDays(month.getValue());
      ventReport123Month.setComments("comment" + month.getValue());
      ventReport123Month.setCategory1(BigDecimal.valueOf(month.getValue()));

      ventReport123Months.add(ventReport123Month);
    }
    return ventReport123Months;
  }

  public static List<VentReport123Month> getVentReport123MonthsForSplitYear(ApplicationVersion applicationVersion, int startYear) {
    List<VentReport123Month> ventReport123Months = new ArrayList<>();

    VentReport123Month ventReport123Month;
    for (Month month : Month.values()) {
      var year = month.getValue() < 6 ? startYear + 1 : startYear;
      ventReport123Month = new VentReport123Month();
      ventReport123Month.setApplicationVersion(applicationVersion);
      ventReport123Month.setYear(year);
      ventReport123Month.setMonth(month);
      ventReport123Month.setShutDownDays(month.getValue());
      ventReport123Month.setComments("comment" + month.getValue());
      ventReport123Month.setCategory1(BigDecimal.valueOf(month.getValue()));

      ventReport123Months.add(ventReport123Month);
    }
    return ventReport123Months;
  }
}
