package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class ReportUtilTest {

  @Test
  void getExpectedYearMonthsForPeriod_endNow() {
    YearMonth startYearMonth = YearMonth.now().minusMonths(11);
    YearMonth endYearMonth = YearMonth.now();

    assertThat(ReportUtil.getExpectedYearMonthsForPeriod(startYearMonth, endYearMonth))
        .containsExactly(startYearMonth,
            startYearMonth.plusMonths(1),
            startYearMonth.plusMonths(2),
            startYearMonth.plusMonths(3),
            startYearMonth.plusMonths(4),
            startYearMonth.plusMonths(5),
            startYearMonth.plusMonths(6),
            startYearMonth.plusMonths(7),
            startYearMonth.plusMonths(8),
            startYearMonth.plusMonths(9),
            startYearMonth.plusMonths(10),
            endYearMonth);
  }

  @Test
  void getExpectedYearMonthsForPeriod_nullStartAndEnd() {
    assertThatThrownBy(() -> ReportUtil.getExpectedYearMonthsForPeriod(null, null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void getSetOfExpectedYearMonthsForPeriod_startNow() {
    YearMonth startYearMonth = YearMonth.now();
    YearMonth endYearMonth = YearMonth.now().plusMonths(11);

    assertThat(ReportUtil.getSetOfExpectedYearMonthsForPeriod(startYearMonth, endYearMonth))
        .containsOnly(startYearMonth,
            startYearMonth.plusMonths(1),
            startYearMonth.plusMonths(2),
            startYearMonth.plusMonths(3),
            startYearMonth.plusMonths(4),
            startYearMonth.plusMonths(5),
            startYearMonth.plusMonths(6),
            startYearMonth.plusMonths(7),
            startYearMonth.plusMonths(8),
            startYearMonth.plusMonths(9),
            startYearMonth.plusMonths(10),
            endYearMonth);
  }

  @Test
  void getSetOfExpectedYearMonthsForPeriod_nullStartAndEnd() {
    assertThatThrownBy(() -> ReportUtil.getSetOfExpectedYearMonthsForPeriod(null, null))
        .isInstanceOf(NullPointerException.class);
  }

}
