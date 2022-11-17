package uk.co.nstauthority.fieldconsents.formatting;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

public class DateUtils {

  public static final String SHORT_DATE = "dd MMM yyyy";

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

  public static Map<String, String> monthsMap() {
    return Arrays.stream(Month.values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, item -> item.getDisplayName(TextStyle.FULL, Locale.ENGLISH)));
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
}
