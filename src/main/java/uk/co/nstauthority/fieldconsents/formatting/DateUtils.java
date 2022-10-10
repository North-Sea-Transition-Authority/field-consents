package uk.co.nstauthority.fieldconsents.formatting;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {

  public static final String SHORT_DATE = "dd MMM yyyy";

  private DateUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static String format(LocalDate date, String format) {
    DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
    return date != null ? customFormatter.format(date) : "";
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
