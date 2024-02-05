package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.ventreport;

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
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123Row;

@ExtendWith(MockitoExtension.class)
class VentReport123ServiceTest {

  @Mock
  private VentReport123MonthRepository ventReport123MonthRepository;

  @InjectMocks
  private VentReport123Service ventReport123Service;

  private ApplicationVersion applicationVersion;

  private List<VentReport123Month> ventReport123MonthsOrdered;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    var ventReport123Months = VentReport123TestUtil.getVentReport123MonthsForSplitYear(
        applicationVersion, 2022);
    ventReport123MonthsOrdered = ventReport123Months
        .stream()
        .sorted(Comparator
            .comparing(VentReport123Month::getYear)
            .thenComparing(Vent123Row::getMonth))
        .toList();
    when(ventReport123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventReport123Months);
  }

  @Test
  void getVentReport123Months() {
    assertThat(ventReport123Service.getVentReport123Months(applicationVersion))
        .isEqualTo(ventReport123MonthsOrdered);
  }

  @Test
  void getStartYearMonth() {
    var startYearMonth = YearMonth.of(ventReport123MonthsOrdered.get(0).getYear(),
        ventReport123MonthsOrdered.get(0).getMonth());

    assertThat(ventReport123Service.getStartYearMonth(applicationVersion))
        .isEqualTo(startYearMonth);
  }

  @Test
  void getStartYearMonth_expectThrow() {
    when(ventReport123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> ventReport123Service.getStartYearMonth(applicationVersion))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void getEndYearMonth() {
    var monthsCount = ventReport123MonthsOrdered.size();
    var endYearMonth = YearMonth.of(ventReport123MonthsOrdered.get(monthsCount - 1).getYear(),
        ventReport123MonthsOrdered.get(monthsCount - 1).getMonth());

    assertThat(ventReport123Service.getEndYearMonth(applicationVersion))
        .isEqualTo(endYearMonth);
  }

  @Test
  void getEndYearMonth_expectThrow() {
    when(ventReport123MonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> ventReport123Service.getEndYearMonth(applicationVersion))
        .isInstanceOf(NoSuchElementException.class);
  }
}
