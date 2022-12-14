package uk.co.nstauthority.fieldconsents.flarevent;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReportUtil {

  private ReportUtil() {
    throw new IllegalStateException("ReportUtil is a util class and should not be instantiated");
  }

  public static List<YearMonth> getExpectedYearMonthsForPeriod(YearMonth startYearMonth, YearMonth endYearMonth) {

    List<YearMonth> expectedYearMonths = new ArrayList<>();
    for (YearMonth yearMonth = startYearMonth;
         yearMonth.isBefore(endYearMonth.plusMonths(1));
         yearMonth = yearMonth.plusMonths(1)) {
      expectedYearMonths.add(yearMonth);
    }

    return expectedYearMonths;
  }

  public static Set<YearMonth> getSetOfExpectedYearMonthsForPeriod(YearMonth startYearMonth, YearMonth endYearMonth) {
    return new HashSet<>(getExpectedYearMonthsForPeriod(startYearMonth, endYearMonth));
  }

}