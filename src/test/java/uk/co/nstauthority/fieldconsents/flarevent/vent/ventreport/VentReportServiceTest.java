package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTestUtil;

@ExtendWith(MockitoExtension.class)
class VentReportServiceTest {

  @Mock
  private VentReportMonthRepository ventReportMonthRepository;

  @Mock
  private VentReportPeriodService ventReportPeriodService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private EmissionReportSummaryService emissionReportSummaryService;

  private VentReportService ventReportService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    ventReportService = new VentReportService(ventReportMonthRepository, ventReportPeriodService,
        applicationUnitService, emissionReportSummaryService);
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT);
  }

  @Test
  void ventReportMonthsComplete_noPeriod() {
    when(ventReportPeriodService.findVentReportPeriod(applicationVersion)).thenReturn(Optional.empty());

    assertThat(ventReportService.ventReportMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void ventReportMonthsComplete_noReportMonths() {
    VentReportPeriod ventReportPeriod =
        new VentReportPeriod(applicationVersion, Month.JANUARY, 2023);

    when(ventReportPeriodService.findVentReportPeriod(applicationVersion))
        .thenReturn(Optional.of(ventReportPeriod));
    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    assertThat(ventReportService.ventReportMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void ventReportMonthsComplete_reportMonthsMissing() {
    List<VentReportMonth> ventReportMonths =
        VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, 2022);

    VentReportPeriod ventReportPeriod =
        new VentReportPeriod(applicationVersion, Month.JANUARY, 2023);

    when(ventReportPeriodService.findVentReportPeriod(applicationVersion))
        .thenReturn(Optional.of(ventReportPeriod));
    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(ventReportMonths);

    assertThat(ventReportService.ventReportMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void ventReportMonthsComplete_reportMonthsAlign() {
    List<VentReportMonth> ventReportMonths =
        VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, 2022);

    VentReportPeriod ventReportPeriod =
        new VentReportPeriod(applicationVersion, Month.DECEMBER, 2022);

    when(ventReportPeriodService.findVentReportPeriod(applicationVersion))
        .thenReturn(Optional.of(ventReportPeriod));
    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(ventReportMonths);

    assertThat(ventReportService.ventReportMonthsComplete(applicationVersion)).isTrue();
  }

  @Test
  void getVentReportForm_noExistingData() {
    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());

    YearMonth yearMonthNow = YearMonth.now();
    VentReportPeriod ventReportPeriod =
        new VentReportPeriod(applicationVersion, yearMonthNow.getMonth(), yearMonthNow.getYear());

    when(ventReportPeriodService.getVentReportPeriodOrError(applicationVersion))
        .thenReturn(ventReportPeriod);

    YearMonth startYearMonth = ventReportPeriod.getReportStartYearMonth();

    VentReportForm ventReportForm = ventReportService.getVentReportForm(applicationVersion);

    assertThat(ventReportForm)
        .extracting(VentReportForm::getStartYear,
            VentReportForm::getEndYear)
        .containsExactly(String.valueOf(startYearMonth.getYear()),
            String.valueOf(yearMonthNow.getYear()));

    assertThat(ventReportForm.getVentReportMonthForms())
        .extracting(VentReportMonthForm::getYear,
            VentReportMonthForm::getMonth,
            form -> form.getShutDownDays().getInputValue(),
            form -> form.getComments().getInputValue(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getInputValue())
        .containsExactly(
            tuple(String.valueOf(startYearMonth.getYear()),
                startYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(1).getYear()),
                startYearMonth.plusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(2).getYear()),
                startYearMonth.plusMonths(2).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(3).getYear()),
                startYearMonth.plusMonths(3).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(4).getYear()),
                startYearMonth.plusMonths(4).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(5).getYear()),
                startYearMonth.plusMonths(5).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(6).getYear()),
                startYearMonth.plusMonths(6).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(7).getYear()),
                startYearMonth.plusMonths(7).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(8).getYear()),
                startYearMonth.plusMonths(8).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(9).getYear()),
                startYearMonth.plusMonths(9).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(10).getYear()),
                startYearMonth.plusMonths(10).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(11).getYear()),
                startYearMonth.plusMonths(11).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null));
  }

  @Test
  void getVentReportForm_sixMonthsOfExistingDataOverlapping() {

    List<VentReportMonth> ventReportMonths =
        VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, 2022);

    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventReportMonths);

    YearMonth endYearMonth = YearMonth.of(2023, Month.JUNE);
    VentReportPeriod ventReportPeriod =
        new VentReportPeriod(applicationVersion, endYearMonth.getMonth(), endYearMonth.getYear());

    when(ventReportPeriodService.getVentReportPeriodOrError(applicationVersion))
        .thenReturn(ventReportPeriod);

    YearMonth startYearMonth = ventReportPeriod.getReportStartYearMonth();

    VentReportForm ventReportForm = ventReportService.getVentReportForm(applicationVersion);

    assertThat(ventReportForm)
        .extracting(VentReportForm::getStartYear,
            VentReportForm::getEndYear)
        .containsExactly(String.valueOf(startYearMonth.getYear()),
            String.valueOf(endYearMonth.getYear()));

    assertThat(ventReportForm.getVentReportMonthForms())
        .extracting(VentReportMonthForm::getYear,
            VentReportMonthForm::getMonth,
            form -> form.getShutDownDays().getInputValue(),
            form -> form.getComments().getInputValue(),
            form -> form.getCategoryA().getInputValue(),
            form -> form.getCategoryB().getInputValue(),
            form -> form.getCategoryC().getInputValue())
        .containsExactly(
            tuple(String.valueOf(startYearMonth.getYear()),
                startYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                "7", "comment7", "7", "70", "700"),
            tuple(String.valueOf(startYearMonth.plusMonths(1).getYear()),
                startYearMonth.plusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                "8", "comment8", "8", "80", "800"),
            tuple(String.valueOf(startYearMonth.plusMonths(2).getYear()),
                startYearMonth.plusMonths(2).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                "9", "comment9", "9", "90", "900"),
            tuple(String.valueOf(startYearMonth.plusMonths(3).getYear()),
                startYearMonth.plusMonths(3).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                "10", "comment10", "10", "100", "1000"),
            tuple(String.valueOf(startYearMonth.plusMonths(4).getYear()),
                startYearMonth.plusMonths(4).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                "11", "comment11", "11", "110", "1100"),
            tuple(String.valueOf(startYearMonth.plusMonths(5).getYear()),
                startYearMonth.plusMonths(5).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                "12", "comment12", "12", "120", "1200"),
            tuple(String.valueOf(startYearMonth.plusMonths(6).getYear()),
                startYearMonth.plusMonths(6).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(7).getYear()),
                startYearMonth.plusMonths(7).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(8).getYear()),
                startYearMonth.plusMonths(8).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(9).getYear()),
                startYearMonth.plusMonths(9).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(10).getYear()),
                startYearMonth.plusMonths(10).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null),
            tuple(String.valueOf(startYearMonth.plusMonths(11).getYear()),
                startYearMonth.plusMonths(11).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                null, null, null, null, null));
  }

  @Test
  void saveVentReport() {
    ventReportService.saveVentReport(applicationVersion, VentReportTestUtil.getFullVentReportFormForYear(2022));

    verify(ventReportMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);

    ArgumentCaptor<VentReportMonth> ventReportMonthArgumentCaptor = ArgumentCaptor.forClass(VentReportMonth.class);
    verify(ventReportMonthRepository, times(12)).save(ventReportMonthArgumentCaptor.capture());
  }

  @Test
  void getVentReportSummaryCards_noPeriodExists() {
    when(ventReportPeriodService.findVentReportPeriod(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(ventReportService.getVentReportSummaryCards(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCardList());
  }

  @Test
  void getVentReportSummaryCards_periodExists_noReportMonths() {
    var ventReportPeriod = VentReportTestUtil.getFullVentReportPeriod();
    var simpleSummaryCard = SummaryTestUtil.getSimpleSummaryCard();

    when(ventReportPeriodService.findVentReportPeriod(applicationVersion))
        .thenReturn(Optional.of(ventReportPeriod));
    when(emissionReportSummaryService.getReportPeriodSummaryCard(ventReportPeriod, ApplicationType.VENT))
        .thenReturn(simpleSummaryCard);
    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(ventReportService.getVentReportSummaryCards(applicationVersion))
        .isEqualTo(List.of(simpleSummaryCard));
  }

  @Test
  void getVentReportSummaryCards_periodExists_reportMonthsExist() {
    var ventReportPeriod = VentReportTestUtil.getFullVentReportPeriod();
    var ventReportMonths = VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, ventReportPeriod.getReportEndYear());
    var simpleSummaryCard = SummaryTestUtil.getSimpleSummaryCard();
    var tableSummaryCard = SummaryTestUtil.getTableSummaryCard();
    var ventCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var ventAverageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(ventReportPeriodService.findVentReportPeriod(applicationVersion))
        .thenReturn(Optional.of(ventReportPeriod));
    when(emissionReportSummaryService.getReportPeriodSummaryCard(ventReportPeriod, ApplicationType.VENT))
        .thenReturn(simpleSummaryCard);
    when(ventReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ventReportMonths);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion))
        .thenReturn(ventCategoryUnit);
    when(applicationUnitService.getVentAverageUnit(applicationVersion))
        .thenReturn(ventAverageUnit);
    when(emissionReportSummaryService.getReportTableSummaryCard(ventReportMonths, ventCategoryUnit, ventAverageUnit))
        .thenReturn(tableSummaryCard);

    assertThat(ventReportService.getVentReportSummaryCards(applicationVersion))
        .isEqualTo(List.of(simpleSummaryCard, tableSummaryCard));
  }
}
