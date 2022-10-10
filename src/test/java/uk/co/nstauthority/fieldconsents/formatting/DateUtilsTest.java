package uk.co.nstauthority.fieldconsents.formatting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
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