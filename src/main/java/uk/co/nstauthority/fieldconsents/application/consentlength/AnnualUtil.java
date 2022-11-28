package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.Month;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

public class AnnualUtil {

  private AnnualUtil() {
    throw new IllegalStateException("AnnualUtil is a util class and should not be instantiated");
  }

  public static List<YearMonth> getExpectedYearMonths(Integer year) {
    // produce a list of expected YearMonths for an annual application of the year supplied
    // i.e. what YearMonths should we capture consent data for
    return Arrays.stream(Month.values()).map(month -> YearMonth.of(year, month)).toList();
  }

}
