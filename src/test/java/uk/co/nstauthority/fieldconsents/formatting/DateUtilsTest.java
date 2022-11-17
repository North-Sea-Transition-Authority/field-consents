package uk.co.nstauthority.fieldconsents.formatting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

  private static final LocalDate FIRST_DATE = LocalDate.of(2022, Month.OCTOBER, 1);

  private static final LocalDate SECOND_DATE = LocalDate.of(2022, Month.OCTOBER, 31);

  private static final LocalDate THIRD_DATE = LocalDate.of(2022, Month.DECEMBER, 8);

  @Test
  void format() {
    String firstDateFormatted = "01 Oct 2022";
    assertEquals(firstDateFormatted, DateUtils.format(FIRST_DATE, DateUtils.SHORT_DATE));
  }

  @Test
  void format_yearMonth() {
    assertThat(DateUtils.format(YearMonth.of(2022, Month.JANUARY), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("January 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.FEBRUARY), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("February 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.MARCH), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("March 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.APRIL), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("April 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.MAY), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("May 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.JUNE), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("June 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.JULY), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("July 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.AUGUST), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("August 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.SEPTEMBER), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("September 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.OCTOBER), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("October 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.NOVEMBER), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("November 2022");
    assertThat(DateUtils.format(YearMonth.of(2022, Month.DECEMBER), DateUtils.LONG_MONTH_YEAR))
        .isEqualTo("December 2022");
    assertThat(DateUtils.format((YearMonth) null, DateUtils.LONG_MONTH_YEAR))
        .isEmpty();
  }

  @Test
  void monthsMap() {
    assertThat(DateUtils.monthsMap()).containsExactly(
        entry("JANUARY", "January"),
        entry("FEBRUARY", "February"),
        entry("MARCH", "March"),
        entry("APRIL", "April"),
        entry("MAY", "May"),
        entry("JUNE", "June"),
        entry("JULY", "July"),
        entry("AUGUST", "August"),
        entry("SEPTEMBER", "September"),
        entry("OCTOBER", "October"),
        entry("NOVEMBER", "November"),
        entry("DECEMBER", "December")
    );
  }

  @Test
  void isSameMonth_whenTrue() {
    assertTrue(DateUtils.isSameMonth(FIRST_DATE, SECOND_DATE));
  }

  @Test
  void isSameMonth_whenFalse() {
    assertFalse(DateUtils.isSameMonth(FIRST_DATE, THIRD_DATE));
  }

  @Test
  void min() {
    assertEquals(FIRST_DATE, DateUtils.min(FIRST_DATE, SECOND_DATE));
    assertEquals(FIRST_DATE, DateUtils.min(FIRST_DATE, THIRD_DATE));
    assertEquals(SECOND_DATE, DateUtils.min(SECOND_DATE, THIRD_DATE));
  }

  @Test
  void max() {
    assertEquals(SECOND_DATE, DateUtils.max(FIRST_DATE, SECOND_DATE));
    assertEquals(THIRD_DATE, DateUtils.max(FIRST_DATE, THIRD_DATE));
    assertEquals(THIRD_DATE, DateUtils.max(SECOND_DATE, THIRD_DATE));
  }
}