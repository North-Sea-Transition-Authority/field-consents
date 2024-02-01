package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.shortterm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ShortTermUtil;

public class FlareShortTerm123TestUtil {

  public static List<FlareShortTerm123Month> getFlareShortTerm123MonthsForPeriod(ApplicationVersion applicationVersion,
                                                                                 LocalDate startDate, LocalDate endDate) {
    List<FlareShortTerm123Month> flareShortTerm123Months = new ArrayList<>();

    FlareShortTerm123Month flareShortTerm123Month;
    int rowNumber = 1;
    for (Pair<LocalDate, LocalDate> shortTermMonth: ShortTermUtil.getExpectedMonthTerms(startDate, endDate)) {
      flareShortTerm123Month = new FlareShortTerm123Month();
      flareShortTerm123Month.setApplicationVersion(applicationVersion);
      flareShortTerm123Month.setStartDate(shortTermMonth.getLeft());
      flareShortTerm123Month.setEndDate(shortTermMonth.getRight());
      flareShortTerm123Month.setYear(flareShortTerm123Month.getStartDate().getYear());
      flareShortTerm123Month.setMonth(flareShortTerm123Month.getStartDate().getMonth());
      flareShortTerm123Month.setComments("comment" + rowNumber);
      flareShortTerm123Month.setCategory1(BigDecimal.valueOf(rowNumber));
      flareShortTerm123Month.setCategory2(BigDecimal.valueOf(rowNumber * 10));
      flareShortTerm123Month.setCategory3(BigDecimal.valueOf(rowNumber * 100));

      flareShortTerm123Months.add(flareShortTerm123Month);
      rowNumber++;
    }
    return flareShortTerm123Months;
  }
}
