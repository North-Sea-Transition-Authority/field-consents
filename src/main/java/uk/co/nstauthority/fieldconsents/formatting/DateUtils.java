package uk.co.nstauthority.fieldconsents.formatting;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

public class DateUtils {

  public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
  public static final Locale DEFAULT_LOCALE = Locale.UK;

  public static final String SHORT_DATE = uk.co.fivium.formlibrary.validator.date.DateUtils.SHORT_DATE;
  public static final String LONG_DATE = uk.co.fivium.formlibrary.validator.date.DateUtils.LONG_DATE;

  public static final String DATE_TIME = "d MMM yyyy HH:mm";

  public static final String LONG_MONTH_YEAR = "MMMM yyyy";

  public static final String DATE_PICKER_FORMAT = "dd/MM/yyyy";

  public static final String DATE_PICKER_WITH_TIME_FORMAT = DATE_PICKER_FORMAT + " H:m";

  private DateUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static String format(Month month, TextStyle textStyle) {
    return month.getDisplayName(textStyle, DEFAULT_LOCALE);
  }

  public static String format(TemporalAccessor temporalAccessor, String format) {
    if (Objects.isNull(temporalAccessor)) {
      return "";
    }

    return DateTimeFormatter
        .ofPattern(format)
        .withZone(DEFAULT_ZONE_ID)
        .withLocale(DEFAULT_LOCALE)
        .format(temporalAccessor);
  }

  public static String formatShort(Month month) {
    return format(month, TextStyle.SHORT);
  }

  public static String formatFull(Month month) {
    return format(month, TextStyle.FULL);
  }

  public static String formatShort(Month month, Integer year) {
    return formatShort(month) + " " + year;
  }

  public static Map<String, String> monthsMap() {
    return Arrays.stream(Month.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, DateUtils::formatFull));
  }

  public static boolean isSameMonth(LocalDate startTermDate, LocalDate endTermDate) {
    return startTermDate.getMonth().equals(endTermDate.getMonth()) && startTermDate.getYear() == endTermDate.getYear();
  }

  public static LocalDate min(LocalDate firstDate, LocalDate secondDate) {
    return firstDate.isBefore(secondDate)
        ? firstDate
        : secondDate;
  }

  public static LocalDate max(LocalDate firstDate, LocalDate secondDate) {
    return secondDate.isAfter(firstDate)
        ? secondDate
        : firstDate;
  }

  /**
   * Find the number of days inclusive between two dates in the same month and year.
   * @param startDateInclusive The start date (inclusive)
   * @param endDateInclusive The end date (inclusive)
   * @return The number of days between to dates in the same month and year.
   */
  public static int daysBetweenInclusive(LocalDate startDateInclusive, LocalDate endDateInclusive) {
    if (YearMonth.from(startDateInclusive).equals(YearMonth.from(endDateInclusive))) {
      return Period.between(startDateInclusive, endDateInclusive).getDays() + 1;
    } else {
      throw new RuntimeException("The start date %s and end date %s should be in the same month and year"
          .formatted(startDateInclusive, endDateInclusive));
    }
  }


  public static LocalDate datePickerStringToDate(String dateStr) {
    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(DATE_PICKER_FORMAT));
  }

  public static String constructDatePickerWithTimeString(String dateStr, String hoursStr, String minutesStr) {
    return "%s %s:%s".formatted(dateStr, hoursStr, minutesStr);
  }

  public static ZonedDateTime datePickerWithTimeStringToDateTime(String dateStr, String hoursStr, String minutesStr) {
    var dateTimeStr = constructDatePickerWithTimeString(dateStr, hoursStr, minutesStr);
    return datePickerWithTimeStringToDateTime(dateTimeStr);
  }

  public static ZonedDateTime datePickerWithTimeStringToDateTime(String dateTimeStr) {
    var formatter = DateTimeFormatter.ofPattern(DATE_PICKER_WITH_TIME_FORMAT);
    return LocalDateTime.parse(dateTimeStr, formatter).atZone(DEFAULT_ZONE_ID);
  }

  public static Instant datePickerWithTimeStringToInstant(String dateStr, String hoursStr, String minutesStr) {
    var dateTimeStr = constructDatePickerWithTimeString(dateStr, hoursStr, minutesStr);
    return datePickerWithTimeStringToInstant(dateTimeStr);
  }

  public static Instant datePickerWithTimeStringToInstant(String dateTimeStr) {
    return datePickerWithTimeStringToDateTime(dateTimeStr).toInstant();
  }
}
