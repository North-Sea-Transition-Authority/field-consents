package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123Row;

@ExtendWith(MockitoExtension.class)
class FlareReport123ServiceTest {

  @Mock
  private FlareReport123MonthRepository flareReport123MonthRepository;

  @InjectMocks
  private FlareReport123Service flareReport123Service;

  private ApplicationVersion applicationVersion;
  private List<FlareReport123Month> flareReport123MonthsOrdered;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    var flareReport123Months = FlareReport123TestUtil.getFlareReport123MonthsForSplitYear(
        applicationVersion, 2022);
    flareReport123MonthsOrdered = flareReport123Months
        .stream()
        .sorted(Comparator
            .comparing(Flare123Row::getYear)
            .thenComparing(Flare123Row::getMonth))
        .toList();
    when(flareReport123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareReport123Months);
  }

  @Test
  void getFlareReport123Months() {
    assertThat(flareReport123Service.getFlareReport123Months(applicationVersion))
        .isEqualTo(flareReport123MonthsOrdered);
  }

  @Test
  void getStartYearMonth() {
    var startYearMonth = YearMonth.of(flareReport123MonthsOrdered.get(0).getYear(),
        flareReport123MonthsOrdered.get(0).getMonth());

    assertThat(flareReport123Service.getStartYearMonth(applicationVersion))
        .isEqualTo(startYearMonth);
  }

  @Test
  void getStartYearMonth_expectThrow() {
    when(flareReport123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> flareReport123Service.getStartYearMonth(applicationVersion))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void getEndYearMonth() {
    var monthsCount = flareReport123MonthsOrdered.size();
    var endYearMonth = YearMonth.of(flareReport123MonthsOrdered.get(monthsCount - 1).getYear(),
        flareReport123MonthsOrdered.get(monthsCount - 1).getMonth());

    assertThat(flareReport123Service.getEndYearMonth(applicationVersion))
        .isEqualTo(endYearMonth);
  }

  @Test
  void getEndYearMonth_expectThrow() {
    when(flareReport123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> flareReport123Service.getEndYearMonth(applicationVersion))
        .isInstanceOf(NoSuchElementException.class);
  }
}
