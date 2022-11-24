package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public class ShortTermUtil {

  private ShortTermUtil() {
    throw new IllegalStateException("ShortTermUtil is a util class and should not be instantiated");
  }

  public static List<Pair<LocalDate, LocalDate>> getExpectedMonthTerms(LocalDate startTermDate,
                                                                       LocalDate endTermDate) {
    YearMonth startYearMonth = YearMonth.of(startTermDate.getYear(), startTermDate.getMonth());
    YearMonth endYearMonth = YearMonth.of(endTermDate.getYear(), endTermDate.getMonth());

    // create a list of expected date pairs based on the start and end dates
    List<Pair<LocalDate, LocalDate>> expectedMonthTerms = new ArrayList<>();
    for (YearMonth yearMonth = startYearMonth;
         yearMonth.isBefore(endYearMonth.plusMonths(1));
         yearMonth = yearMonth.plusMonths(1)) {
      expectedMonthTerms.add(Pair.of(DateUtils.max(startTermDate, yearMonth.atDay(1)),
          DateUtils.min(endTermDate, yearMonth.atEndOfMonth())));
    }
    return expectedMonthTerms;
  }

}
