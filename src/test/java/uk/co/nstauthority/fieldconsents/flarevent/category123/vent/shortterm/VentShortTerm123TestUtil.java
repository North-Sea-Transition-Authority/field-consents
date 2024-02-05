package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.shortterm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ShortTermUtil;

public class VentShortTerm123TestUtil {

  public static List<VentShortTerm123Month> getVentShortTerm123MonthsForPeriod(ApplicationVersion applicationVersion,
                                                                               LocalDate startDate, LocalDate endDate) {
    List<VentShortTerm123Month> ventShortTerm123Months = new ArrayList<>();

    VentShortTerm123Month ventShortTerm123Month;
    int rowNumber = 1;
    for (Pair<LocalDate, LocalDate> shortTermMonth: ShortTermUtil.getExpectedMonthTerms(startDate, endDate)) {
      ventShortTerm123Month = new VentShortTerm123Month();
      ventShortTerm123Month.setApplicationVersion(applicationVersion);
      ventShortTerm123Month.setStartDate(shortTermMonth.getLeft());
      ventShortTerm123Month.setEndDate(shortTermMonth.getRight());
      ventShortTerm123Month.setYear(ventShortTerm123Month.getStartDate().getYear());
      ventShortTerm123Month.setMonth(ventShortTerm123Month.getStartDate().getMonth());
      ventShortTerm123Month.setComments("comment" + rowNumber);
      ventShortTerm123Month.setCategory1(BigDecimal.valueOf(rowNumber));

      ventShortTerm123Months.add(ventShortTerm123Month);
      rowNumber++;
    }
    return ventShortTerm123Months;
  }
}
