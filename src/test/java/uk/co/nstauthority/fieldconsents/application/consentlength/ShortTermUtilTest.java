package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class ShortTermUtilTest {

  @Test
  void getExpectedMonthTerms_multiMonthMiddleAcrossYears() {
    var startDate = LocalDate.of(2022, Month.OCTOBER, 31);
    var endDate = LocalDate.of(2023, Month.APRIL, 12);

    List<Pair<LocalDate, LocalDate>> expectedShortTermMonthTerms =
        ShortTermUtil.getExpectedMonthTerms(startDate, endDate);

    assertThat(expectedShortTermMonthTerms)
        .extracting(Pair::getLeft, Pair::getRight)
        .containsExactly(
            tuple(startDate, startDate),
            tuple(LocalDate.of(2022, Month.NOVEMBER, 1),
                LocalDate.of(2022, Month.NOVEMBER, 30)),
            tuple(LocalDate.of(2022, Month.DECEMBER, 1),
                LocalDate.of(2022, Month.DECEMBER, 31)),
            tuple(LocalDate.of(2023, Month.JANUARY, 1),
                LocalDate.of(2023, Month.JANUARY, 31)),
            tuple(LocalDate.of(2023, Month.FEBRUARY, 1),
                LocalDate.of(2023, Month.FEBRUARY, 28)),
            tuple(LocalDate.of(2023, Month.MARCH, 1),
                LocalDate.of(2023, Month.MARCH, 31)),
            tuple(LocalDate.of(2023, Month.APRIL, 1),
                endDate));
  }

  @Test
  void getExpectedMonthTerms_multiMonthWholeMonths() {
    var startDate = LocalDate.of(2022, Month.OCTOBER, 1);
    var endDate = LocalDate.of(2023, Month.APRIL, 30);

    List<Pair<LocalDate, LocalDate>> expectedShortTermMonthTerms =
        ShortTermUtil.getExpectedMonthTerms(startDate, endDate);

    assertThat(expectedShortTermMonthTerms)
        .extracting(Pair::getLeft, Pair::getRight)
        .containsExactly(
            tuple(startDate, LocalDate.of(2022, Month.OCTOBER, 31)),
            tuple(LocalDate.of(2022, Month.NOVEMBER, 1),
                LocalDate.of(2022, Month.NOVEMBER, 30)),
            tuple(LocalDate.of(2022, Month.DECEMBER, 1),
                LocalDate.of(2022, Month.DECEMBER, 31)),
            tuple(LocalDate.of(2023, Month.JANUARY, 1),
                LocalDate.of(2023, Month.JANUARY, 31)),
            tuple(LocalDate.of(2023, Month.FEBRUARY, 1),
                LocalDate.of(2023, Month.FEBRUARY, 28)),
            tuple(LocalDate.of(2023, Month.MARCH, 1),
                LocalDate.of(2023, Month.MARCH, 31)),
            tuple(LocalDate.of(2023, Month.APRIL, 1),
                endDate));
  }

  @Test
  void getExpectedMonthTerms_multiMonthWholeMonthsSingleYear() {
    var startDate = LocalDate.of(2022, Month.JANUARY, 1);
    var endDate = LocalDate.of(2022, Month.DECEMBER, 31);

    List<Pair<LocalDate, LocalDate>> expectedShortTermMonthTerms =
        ShortTermUtil.getExpectedMonthTerms(startDate, endDate);

    assertThat(expectedShortTermMonthTerms)
        .extracting(Pair::getLeft, Pair::getRight)
        .containsExactly(
            tuple(startDate,
                LocalDate.of(2022, Month.JANUARY, 31)),
            tuple(LocalDate.of(2022, Month.FEBRUARY, 1),
                LocalDate.of(2022, Month.FEBRUARY, 28)),
            tuple(LocalDate.of(2022, Month.MARCH, 1),
                LocalDate.of(2022, Month.MARCH, 31)),
            tuple(LocalDate.of(2022, Month.APRIL, 1),
                LocalDate.of(2022, Month.APRIL, 30)),
            tuple(LocalDate.of(2022, Month.MAY, 1),
                LocalDate.of(2022, Month.MAY, 31)),
            tuple(LocalDate.of(2022, Month.JUNE, 1),
                LocalDate.of(2022, Month.JUNE, 30)),
            tuple(LocalDate.of(2022, Month.JULY, 1),
                LocalDate.of(2022, Month.JULY, 31)),
            tuple(LocalDate.of(2022, Month.AUGUST, 1),
                LocalDate.of(2022, Month.AUGUST, 31)),
            tuple(LocalDate.of(2022, Month.SEPTEMBER, 1),
                LocalDate.of(2022, Month.SEPTEMBER, 30)),
            tuple(LocalDate.of(2022, Month.OCTOBER, 1),
                LocalDate.of(2022, Month.OCTOBER, 31)),
            tuple(LocalDate.of(2022, Month.NOVEMBER, 1),
                LocalDate.of(2022, Month.NOVEMBER, 30)),
            tuple(LocalDate.of(2022, Month.DECEMBER, 1),
                endDate));
  }

  @Test
  void getExpectedMonthTerms_multiMonthMiddleMonthsSingleYear() {
    var startDate = LocalDate.of(2022, Month.JANUARY, 15);
    var endDate = LocalDate.of(2022, Month.DECEMBER, 15);

    List<Pair<LocalDate, LocalDate>> expectedShortTermMonthTerms =
        ShortTermUtil.getExpectedMonthTerms(startDate, endDate);

    assertThat(expectedShortTermMonthTerms)
        .extracting(Pair::getLeft, Pair::getRight)
        .containsExactly(
            tuple(startDate,
                LocalDate.of(2022, Month.JANUARY, 31)),
            tuple(LocalDate.of(2022, Month.FEBRUARY, 1),
                LocalDate.of(2022, Month.FEBRUARY, 28)),
            tuple(LocalDate.of(2022, Month.MARCH, 1),
                LocalDate.of(2022, Month.MARCH, 31)),
            tuple(LocalDate.of(2022, Month.APRIL, 1),
                LocalDate.of(2022, Month.APRIL, 30)),
            tuple(LocalDate.of(2022, Month.MAY, 1),
                LocalDate.of(2022, Month.MAY, 31)),
            tuple(LocalDate.of(2022, Month.JUNE, 1),
                LocalDate.of(2022, Month.JUNE, 30)),
            tuple(LocalDate.of(2022, Month.JULY, 1),
                LocalDate.of(2022, Month.JULY, 31)),
            tuple(LocalDate.of(2022, Month.AUGUST, 1),
                LocalDate.of(2022, Month.AUGUST, 31)),
            tuple(LocalDate.of(2022, Month.SEPTEMBER, 1),
                LocalDate.of(2022, Month.SEPTEMBER, 30)),
            tuple(LocalDate.of(2022, Month.OCTOBER, 1),
                LocalDate.of(2022, Month.OCTOBER, 31)),
            tuple(LocalDate.of(2022, Month.NOVEMBER, 1),
                LocalDate.of(2022, Month.NOVEMBER, 30)),
            tuple(LocalDate.of(2022, Month.DECEMBER, 1),
                endDate));
  }

  @Test
  void getExpectedMonthTerms_multiMonthWholeMonthsTwoYears() {
    var startDate = LocalDate.of(2022, Month.JANUARY, 1);
    var endDate = LocalDate.of(2023, Month.DECEMBER, 31);

    List<Pair<LocalDate, LocalDate>> expectedShortTermMonthTerms =
        ShortTermUtil.getExpectedMonthTerms(startDate, endDate);

    assertThat(expectedShortTermMonthTerms)
        .extracting(Pair::getLeft, Pair::getRight)
        .containsExactly(
            tuple(startDate,
                LocalDate.of(2022, Month.JANUARY, 31)),
            tuple(LocalDate.of(2022, Month.FEBRUARY, 1),
                LocalDate.of(2022, Month.FEBRUARY, 28)),
            tuple(LocalDate.of(2022, Month.MARCH, 1),
                LocalDate.of(2022, Month.MARCH, 31)),
            tuple(LocalDate.of(2022, Month.APRIL, 1),
                LocalDate.of(2022, Month.APRIL, 30)),
            tuple(LocalDate.of(2022, Month.MAY, 1),
                LocalDate.of(2022, Month.MAY, 31)),
            tuple(LocalDate.of(2022, Month.JUNE, 1),
                LocalDate.of(2022, Month.JUNE, 30)),
            tuple(LocalDate.of(2022, Month.JULY, 1),
                LocalDate.of(2022, Month.JULY, 31)),
            tuple(LocalDate.of(2022, Month.AUGUST, 1),
                LocalDate.of(2022, Month.AUGUST, 31)),
            tuple(LocalDate.of(2022, Month.SEPTEMBER, 1),
                LocalDate.of(2022, Month.SEPTEMBER, 30)),
            tuple(LocalDate.of(2022, Month.OCTOBER, 1),
                LocalDate.of(2022, Month.OCTOBER, 31)),
            tuple(LocalDate.of(2022, Month.NOVEMBER, 1),
                LocalDate.of(2022, Month.NOVEMBER, 30)),
            tuple(LocalDate.of(2022, Month.DECEMBER, 1),
                LocalDate.of(2022, Month.DECEMBER, 31)),
            tuple(LocalDate.of(2023, Month.JANUARY, 1),
                LocalDate.of(2023, Month.JANUARY, 31)),
            tuple(LocalDate.of(2023, Month.FEBRUARY, 1),
                LocalDate.of(2023, Month.FEBRUARY, 28)),
            tuple(LocalDate.of(2023, Month.MARCH, 1),
                LocalDate.of(2023, Month.MARCH, 31)),
            tuple(LocalDate.of(2023, Month.APRIL, 1),
                LocalDate.of(2023, Month.APRIL, 30)),
            tuple(LocalDate.of(2023, Month.MAY, 1),
                LocalDate.of(2023, Month.MAY, 31)),
            tuple(LocalDate.of(2023, Month.JUNE, 1),
                LocalDate.of(2023, Month.JUNE, 30)),
            tuple(LocalDate.of(2023, Month.JULY, 1),
                LocalDate.of(2023, Month.JULY, 31)),
            tuple(LocalDate.of(2023, Month.AUGUST, 1),
                LocalDate.of(2023, Month.AUGUST, 31)),
            tuple(LocalDate.of(2023, Month.SEPTEMBER, 1),
                LocalDate.of(2023, Month.SEPTEMBER, 30)),
            tuple(LocalDate.of(2023, Month.OCTOBER, 1),
                LocalDate.of(2023, Month.OCTOBER, 31)),
            tuple(LocalDate.of(2023, Month.NOVEMBER, 1),
                LocalDate.of(2023, Month.NOVEMBER, 30)),
            tuple(LocalDate.of(2023, Month.DECEMBER, 1),
                endDate)
        );
  }

  @Test
  void getExpectedMonthTerms_multiMonthMiddleMonthsTwoYears() {
    var startDate = LocalDate.of(2022, Month.JANUARY, 15);
    var endDate = LocalDate.of(2023, Month.DECEMBER, 15);

    List<Pair<LocalDate, LocalDate>> expectedShortTermMonthTerms =
        ShortTermUtil.getExpectedMonthTerms(startDate, endDate);

    assertThat(expectedShortTermMonthTerms)
        .extracting(Pair::getLeft, Pair::getRight)
        .containsExactly(
            tuple(startDate,
                LocalDate.of(2022, Month.JANUARY, 31)),
            tuple(LocalDate.of(2022, Month.FEBRUARY, 1),
                LocalDate.of(2022, Month.FEBRUARY, 28)),
            tuple(LocalDate.of(2022, Month.MARCH, 1),
                LocalDate.of(2022, Month.MARCH, 31)),
            tuple(LocalDate.of(2022, Month.APRIL, 1),
                LocalDate.of(2022, Month.APRIL, 30)),
            tuple(LocalDate.of(2022, Month.MAY, 1),
                LocalDate.of(2022, Month.MAY, 31)),
            tuple(LocalDate.of(2022, Month.JUNE, 1),
                LocalDate.of(2022, Month.JUNE, 30)),
            tuple(LocalDate.of(2022, Month.JULY, 1),
                LocalDate.of(2022, Month.JULY, 31)),
            tuple(LocalDate.of(2022, Month.AUGUST, 1),
                LocalDate.of(2022, Month.AUGUST, 31)),
            tuple(LocalDate.of(2022, Month.SEPTEMBER, 1),
                LocalDate.of(2022, Month.SEPTEMBER, 30)),
            tuple(LocalDate.of(2022, Month.OCTOBER, 1),
                LocalDate.of(2022, Month.OCTOBER, 31)),
            tuple(LocalDate.of(2022, Month.NOVEMBER, 1),
                LocalDate.of(2022, Month.NOVEMBER, 30)),
            tuple(LocalDate.of(2022, Month.DECEMBER, 1),
                LocalDate.of(2022, Month.DECEMBER, 31)),
            tuple(LocalDate.of(2023, Month.JANUARY, 1),
                LocalDate.of(2023, Month.JANUARY, 31)),
            tuple(LocalDate.of(2023, Month.FEBRUARY, 1),
                LocalDate.of(2023, Month.FEBRUARY, 28)),
            tuple(LocalDate.of(2023, Month.MARCH, 1),
                LocalDate.of(2023, Month.MARCH, 31)),
            tuple(LocalDate.of(2023, Month.APRIL, 1),
                LocalDate.of(2023, Month.APRIL, 30)),
            tuple(LocalDate.of(2023, Month.MAY, 1),
                LocalDate.of(2023, Month.MAY, 31)),
            tuple(LocalDate.of(2023, Month.JUNE, 1),
                LocalDate.of(2023, Month.JUNE, 30)),
            tuple(LocalDate.of(2023, Month.JULY, 1),
                LocalDate.of(2023, Month.JULY, 31)),
            tuple(LocalDate.of(2023, Month.AUGUST, 1),
                LocalDate.of(2023, Month.AUGUST, 31)),
            tuple(LocalDate.of(2023, Month.SEPTEMBER, 1),
                LocalDate.of(2023, Month.SEPTEMBER, 30)),
            tuple(LocalDate.of(2023, Month.OCTOBER, 1),
                LocalDate.of(2023, Month.OCTOBER, 31)),
            tuple(LocalDate.of(2023, Month.NOVEMBER, 1),
                LocalDate.of(2023, Month.NOVEMBER, 30)),
            tuple(LocalDate.of(2023, Month.DECEMBER, 1),
                endDate)
        );
  }

  @Test
  void getExpectedMonthTerms_oneMonthMiddle() {
    var startDate = LocalDate.of(2022, Month.OCTOBER, 9);
    var endDate = LocalDate.of(2022, Month.OCTOBER, 12);

    List<Pair<LocalDate, LocalDate>> expectedShortTermMonthTerms =
        ShortTermUtil.getExpectedMonthTerms(startDate, endDate);

    assertThat(expectedShortTermMonthTerms)
        .extracting(Pair::getLeft, Pair::getRight)
        .containsExactly(tuple(startDate, endDate));
  }

  @Test
  void getExpectedMonthTerms_oneMonthWhole() {
    var startDate = LocalDate.of(2022, Month.OCTOBER, 1);
    var endDate = LocalDate.of(2022, Month.OCTOBER, 31);

    List<Pair<LocalDate, LocalDate>> expectedShortTermMonthTerms =
        ShortTermUtil.getExpectedMonthTerms(startDate, endDate);

    assertThat(expectedShortTermMonthTerms)
        .extracting(Pair::getLeft, Pair::getRight)
        .containsExactly(tuple(startDate, endDate));
  }

}
