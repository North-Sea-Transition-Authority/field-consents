package uk.co.nstauthority.fieldconsents.formatting;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

public class DateUtils {

  public static final String SHORT_DATE = uk.co.fivium.formlibrary.validator.date.DateUtils.SHORT_DATE;

  public static final String DATE_TIME = "d MMM yyyy HH:mm";

  public static final String LONG_MONTH_YEAR = "MMMM yyyy";

  private DateUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static String format(LocalDate date, String format) {
    DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
    return date != null ? customFormatter.format(date) : "";
  }

  public static String format(YearMonth yearMonth, String format) {
    return yearMonth != null ? yearMonth.format(DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault())) : "";
  }

  public static String format(Month month, TextStyle textStyle) {
    return month.getDisplayName(textStyle, Locale.ENGLISH);
  }

  public static String format(Instant instant, String format) {
    DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
    return instant != null ? customFormatter.format(instant) : "";
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

}
