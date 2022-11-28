package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class AnnualUtilTest {

  @Test
  void getExpectedYearMonths_currentYear() {
    int currentYear = Year.now().getValue();

    assertThat(AnnualUtil.getExpectedYearMonths(currentYear))
        .containsExactly(YearMonth.of(currentYear, Month.JANUARY),
            YearMonth.of(currentYear, Month.FEBRUARY),
            YearMonth.of(currentYear, Month.MARCH),
            YearMonth.of(currentYear, Month.APRIL),
            YearMonth.of(currentYear, Month.MAY),
            YearMonth.of(currentYear, Month.JUNE),
            YearMonth.of(currentYear, Month.JULY),
            YearMonth.of(currentYear, Month.AUGUST),
            YearMonth.of(currentYear, Month.SEPTEMBER),
            YearMonth.of(currentYear, Month.OCTOBER),
            YearMonth.of(currentYear, Month.NOVEMBER),
            YearMonth.of(currentYear, Month.DECEMBER));
  }

  @Test
  void getExpectedYearMonths_nullYear() {
    assertThatThrownBy(() -> AnnualUtil.getExpectedYearMonths(null))
        .isInstanceOf(NullPointerException.class);
  }

}