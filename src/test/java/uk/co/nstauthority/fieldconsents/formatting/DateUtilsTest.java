package uk.co.nstauthority.fieldconsents.formatting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateUtilsTest {

  private static final LocalDate FIRST_DATE = LocalDate.of(2022, Month.OCTOBER, 1);

  private static final LocalDate SECOND_DATE = LocalDate.of(2022, Month.OCTOBER, 31);

  private static final LocalDate THIRD_DATE = LocalDate.of(2022, Month.DECEMBER, 8);

  private static final LocalDateTime FIRST_DATE_TIME = LocalDateTime.of(2022, Month.OCTOBER, 1, 12, 0);

  private static final ZonedDateTime FIRST_ZONED_DATE_TIME = FIRST_DATE_TIME.atZone(ZoneId.systemDefault());

  @Test
  void format_shortDate() {
    String firstDateFormatted = "1 Oct 2022";
    assertEquals(firstDateFormatted, DateUtils.format(FIRST_DATE, DateUtils.SHORT_DATE));
  }

  @Test
  void format_dateTime() {
    String firstDateFormatted = "1 Oct 2022 12:00";
    assertEquals(firstDateFormatted, DateUtils.format(FIRST_ZONED_DATE_TIME.toInstant(), DateUtils.DATE_TIME));
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

  @Test
  void daysBetweenInclusive() {
    var startDate = LocalDate.of(2022, Month.OCTOBER, 1);
    assertThat(DateUtils.daysBetweenInclusive(
        startDate,
        LocalDate.of(2022, Month.OCTOBER, 31)))
        .isEqualTo(31);
    assertThat(DateUtils.daysBetweenInclusive(
        startDate,
        LocalDate.of(2022, Month.OCTOBER, 10)))
        .isEqualTo(10);
    assertThat(DateUtils.daysBetweenInclusive(
        startDate,
        LocalDate.of(2022, Month.OCTOBER, 20)))
        .isEqualTo(20);
  }

  @Test
  void daysBetweenInclusive_differentYearMonth() {
    var startDate = LocalDate.of(2022, Month.OCTOBER, 1);
    var endDateInDifferentMonth = LocalDate.of(2022, Month.NOVEMBER, 20);
    var endDateSameMonthDifferentYear = LocalDate.of(2023, Month.OCTOBER, 20);

    assertThatThrownBy(() ->  DateUtils.daysBetweenInclusive(startDate, endDateInDifferentMonth))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("The start date 2022-10-01 and end date 2022-11-20 should be in the same month and year");

    assertThatThrownBy(() ->  DateUtils.daysBetweenInclusive(startDate, endDateSameMonthDifferentYear))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("The start date 2022-10-01 and end date 2023-10-20 should be in the same month and year");
  }

  @ParameterizedTest
  @MethodSource("getInvalidDatePickerDateStrings")
  void datePickerStringToDate_invalid(String dateStr) {
    assertThatThrownBy(() -> DateUtils.datePickerStringToDate(dateStr))
        .isInstanceOf(DateTimeParseException.class);
  }

  private static Stream<Arguments> getInvalidDatePickerDateStrings() {
    return Stream.of(
        Arguments.of("ab/11/2023"),
        Arguments.of("01/ab/2023"),
        Arguments.of("01/11/abcd"),
        Arguments.of("32/11/2023"),
        Arguments.of("01.11.2023"),
        Arguments.of("01112023"),
        Arguments.of("01-11-2023")
    );
  }

  @ParameterizedTest
  @MethodSource("getValidDatePickerDateElements")
  void datePickerStringToDate_valid(int day, int month, int year) {
    var dateTimeStr = "%02d/%02d/%04d".formatted(day, month, year);

    assertThat(DateUtils.datePickerStringToDate(dateTimeStr)).isEqualTo(LocalDate.of(year, month, day));
  }

  @ParameterizedTest
  @MethodSource("getValidDatePickerDateElements")
  void dateToDatePickerString(int day, int month, int year) {
    var date = LocalDate.of(year, month, day);

    assertThat(DateUtils.dateToDatePickerString(date)).isEqualTo("%02d/%02d/%04d".formatted(day, month, year));
  }

  private static Stream<Arguments> getValidDatePickerDateElements() {
    return Stream.of(
        Arguments.of(1, 2, 2023),
        Arguments.of(11, 12, 23),
        Arguments.of(23, 9, 1),
        Arguments.of(30, 12, 230),
        Arguments.of(1, 1, 1),
        Arguments.of(1, 1, 2023),
        Arguments.of(31, 12, 2023)
    );
  }

  @Test
  void constructDatePickerWithTimeString() {
    var dateStr = "01/02/2023";
    var hoursStr = "12";
    var minutesStr = "59";
    assertThat(DateUtils.constructDatePickerWithTimeString(dateStr, hoursStr, minutesStr))
        .isEqualTo(dateStr + " " + hoursStr + ":" + minutesStr);
  }

  @ParameterizedTest
  @MethodSource("getInvalidDatePickerWithTimeStrings")
  void datePickerWithTimeStringToDateTime_invalid(String dateTimeStr) {
    assertThatThrownBy(() -> DateUtils.datePickerWithTimeStringToDateTime(dateTimeStr))
        .isInstanceOf(DateTimeParseException.class);
  }

  private static Stream<Arguments> getInvalidDatePickerWithTimeStrings() {
    return Stream.of(
        Arguments.of("ab/11/2023 1212"),
        Arguments.of("01/ab/2023 12:12"),
        Arguments.of("01/11/abcd 12:1c"),
        Arguments.of("32/11/202312:12"),
        Arguments.of("01.11.2023 12:12"),
        Arguments.of("01112023 12:12"),
        Arguments.of("01-11-2023 12:12"),
        Arguments.of("01/11/2023 12:61"),
        Arguments.of("01/11/2023 25:15")
    );
  }

  @ParameterizedTest
  @MethodSource("getValidDatePickerWithTimeElements")
  void datePickerWithTimeStringToDateTime_valid(int day, int month, int year, int hours, int minutes) {
    var dateTimeStr = "%02d/%02d/%04d %02d:%02d".formatted(day, month, year, hours, minutes);
    var expectedDateTime = ZonedDateTime.of(year, month, day, hours, minutes, 0, 0, ZoneId.systemDefault());

    assertThat(DateUtils.datePickerWithTimeStringToDateTime(dateTimeStr)).isEqualTo(expectedDateTime);
  }

  private static Stream<Arguments> getValidDatePickerWithTimeElements() {
    return Stream.of(
        Arguments.of(1, 2, 2023, 0, 0),
        Arguments.of(11, 12, 23, 23, 59),
        Arguments.of(23, 9, 1, 1, 1),
        Arguments.of(30, 12, 230, 10, 10),
        Arguments.of(1, 1, 1, 20, 50),
        Arguments.of(1, 1, 2023, 23, 0),
        Arguments.of(31, 12, 2023, 10, 0)
    );
  }

  @ParameterizedTest
  @MethodSource("getInvalidDatePickerAndTimeStrings")
  void datePickerWithTimeStringToDateTime_individualStringElements_invalid(String dateStr, String hoursStr, String minutesStr) {
    assertThatThrownBy(() -> DateUtils.datePickerWithTimeStringToDateTime(dateStr, hoursStr, minutesStr))
        .isInstanceOf(DateTimeParseException.class);
  }

  private static Stream<Arguments> getInvalidDatePickerAndTimeStrings() {
    return Stream.of(
        Arguments.of("ab/11/2023", "12", "12"),
        Arguments.of("01/ab/2023", "12", "12"),
        Arguments.of("01/11/abcd", "12", "1c"),
        Arguments.of("32/11/2023", "12", "12"),
        Arguments.of("01.11.2023", "12", "12"),
        Arguments.of("01112023", "12", "12"),
        Arguments.of("01-11-2023", "12", "12"),
        Arguments.of("01/11/2023", "12", "61"),
        Arguments.of("01/11/2023", "25", "15")
    );
  }

  @ParameterizedTest
  @MethodSource("getValidDatePickerWithTimeElements")
  void datePickerWithTimeStringToDateTime_individualStringElements_valid(int day, int month, int year, int hours, int minutes) {
    var dateTimeStr = "%02d/%02d/%04d %02d:%02d".formatted(day, month, year, hours, minutes);
    var expectedDateTime = ZonedDateTime.of(year, month, day, hours, minutes, 0, 0, ZoneId.systemDefault());

    assertThat(DateUtils.datePickerWithTimeStringToDateTime(dateTimeStr)).isEqualTo(expectedDateTime);
  }

  @ParameterizedTest
  @MethodSource("getInvalidDatePickerWithTimeStrings")
  void datePickerWithTimeStringToInstant_invalid(String dateTimeStr) {
    assertThatThrownBy(() -> DateUtils.datePickerWithTimeStringToInstant(dateTimeStr))
        .isInstanceOf(DateTimeParseException.class);
  }

  @ParameterizedTest
  @MethodSource("getValidDatePickerWithTimeElements")
  void datePickerWithTimeStringToInstant_valid(int day, int month, int year, int hours, int minutes) {
    var dateTimeStr = "%02d/%02d/%04d %02d:%02d".formatted(day, month, year, hours, minutes);
    var expectedInstant = ZonedDateTime.of(year, month, day, hours, minutes, 0, 0, ZoneId.systemDefault()).toInstant();

    assertThat(DateUtils.datePickerWithTimeStringToInstant(dateTimeStr)).isEqualTo(expectedInstant);
  }

  @ParameterizedTest
  @MethodSource("getInvalidDatePickerAndTimeStrings")
  void datePickerWithTimeStringToInstant_individualStringElements_invalid(String dateStr, String hoursStr, String minutesStr) {
    assertThatThrownBy(() -> DateUtils.datePickerWithTimeStringToInstant(dateStr, hoursStr, minutesStr))
        .isInstanceOf(DateTimeParseException.class);
  }

  @ParameterizedTest
  @MethodSource("getValidDatePickerWithTimeElements")
  void datePickerWithTimeStringToInstant_individualStringElements_valid(int day, int month, int year, int hours, int minutes) {
    var dateTimeStr = "%02d/%02d/%04d %02d:%02d".formatted(day, month, year, hours, minutes);
    var expectedInstant = ZonedDateTime.of(year, month, day, hours, minutes, 0, 0, ZoneId.systemDefault()).toInstant();

    assertThat(DateUtils.datePickerWithTimeStringToInstant(dateTimeStr)).isEqualTo(expectedInstant);
  }

  @Test
  void atEndOfDay() {
    var date = LocalDate.now();

    assertThat(DateUtils.atEndOfDay(date))
        .hasYear(date.getYear())
        .hasMonth(date.getMonth())
        .hasDayOfMonth(date.getDayOfMonth())
        .hasHour(23)
        .hasMinute(59)
        .hasSecond(59)
        .hasNano(999_999_999);
  }

  @Test
  void isBeforeOrEqualTo_isBefore() {
    var now = LocalDate.now();
    assertThat(DateUtils.isBeforeOrEqualTo(now.minusDays(1), now)).isTrue();
  }

  @Test
  void isBeforeOrEqualTo_isEqual() {
    var now = LocalDate.now();
    assertThat(DateUtils.isBeforeOrEqualTo(now, now)).isTrue();
  }

  @Test
  void isBeforeOrEqualTo_isAfter() {
    var now = LocalDate.now();
    assertThat(DateUtils.isBeforeOrEqualTo(now.plusDays(1), now)).isFalse();
  }

  @Test
  void isAfterOrEqualTo_isAfter() {
    var now = LocalDate.now();
    assertThat(DateUtils.isAfterOrEqualTo(now.plusDays(1), now)).isTrue();
  }

  @Test
  void isAfterOrEqualTo_isEqual() {
    var now = LocalDate.now();
    assertThat(DateUtils.isAfterOrEqualTo(now, now)).isTrue();
  }

  @Test
  void isAfterOrEqualTo_isBefore() {
    var now = LocalDate.now();
    assertThat(DateUtils.isAfterOrEqualTo(now.minusDays(1), now)).isFalse();
  }

}
