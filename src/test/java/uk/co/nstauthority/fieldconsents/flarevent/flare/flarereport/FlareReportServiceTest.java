package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

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
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTestUtil;

@ExtendWith(MockitoExtension.class)
class FlareReportServiceTest {

  @Mock
  private FlareReportMonthRepository flareReportMonthRepository;

  @Mock
  private FlareReportPeriodService flareReportPeriodService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private EmissionReportSummaryService emissionReportSummaryService;

  private FlareReportService flareReportService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    flareReportService = new FlareReportService(flareReportMonthRepository, flareReportPeriodService,
        applicationUnitService, emissionReportSummaryService);
    applicationVersion = FlareReportTestUtil.flareAppVersion;
  }

  @Test
  void flareReportMonthsComplete_noPeriod() {
    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion)).thenReturn(Optional.empty());

    assertThat(flareReportService.flareReportMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void flareReportMonthsComplete_noReportMonths() {
    FlareReportPeriod flareReportPeriod =
        new FlareReportPeriod(applicationVersion, Month.JANUARY, 2023);

    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion))
        .thenReturn(Optional.of(flareReportPeriod));
    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(new ArrayList<>());

    assertThat(flareReportService.flareReportMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void flareReportMonthsComplete_reportMonthsMissing() {
    List<FlareReportMonth> flareReportMonths =
        FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, 2022);

    FlareReportPeriod flareReportPeriod =
        new FlareReportPeriod(applicationVersion, Month.JANUARY, 2023);

    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion))
        .thenReturn(Optional.of(flareReportPeriod));
    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(flareReportMonths);

    assertThat(flareReportService.flareReportMonthsComplete(applicationVersion)).isFalse();
  }

  @Test
  void flareReportMonthsComplete_reportMonthsAlign() {
    List<FlareReportMonth> flareReportMonths =
        FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, 2022);

    FlareReportPeriod flareReportPeriod =
        new FlareReportPeriod(applicationVersion, Month.DECEMBER, 2022);

    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion))
        .thenReturn(Optional.of(flareReportPeriod));
    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion)).thenReturn(flareReportMonths);

    assertThat(flareReportService.flareReportMonthsComplete(applicationVersion)).isTrue();
  }

  @Test
  void getFlareReportForm_noExistingData() {
    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(new ArrayList<>());

    YearMonth yearMonthNow = YearMonth.now();
    FlareReportPeriod flareReportPeriod =
        new FlareReportPeriod(applicationVersion, yearMonthNow.getMonth(), yearMonthNow.getYear());

    when(flareReportPeriodService.getFlareReportPeriodOrError(applicationVersion))
        .thenReturn(flareReportPeriod);

    YearMonth startYearMonth = flareReportPeriod.getReportStartYearMonth();

    FlareReportForm flareReportForm = flareReportService.getFlareReportForm(applicationVersion);

    assertThat(flareReportForm)
        .extracting(FlareReportForm::getStartYear,
            FlareReportForm::getEndYear)
        .containsExactly(String.valueOf(startYearMonth.getYear()),
            String.valueOf(yearMonthNow.getYear()));

    assertThat(flareReportForm.getFlareReportMonthForms())
        .extracting(FlareReportMonthForm::getYear,
            FlareReportMonthForm::getMonth,
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
  void getFlareReportForm_sixMonthsOfExistingDataOverlapping() {

    List<FlareReportMonth> flareReportMonths =
        FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, 2022);

    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareReportMonths);

    YearMonth endYearMonth = YearMonth.of(2023, Month.JUNE);
    FlareReportPeriod flareReportPeriod =
        new FlareReportPeriod(applicationVersion, endYearMonth.getMonth(), endYearMonth.getYear());

    when(flareReportPeriodService.getFlareReportPeriodOrError(applicationVersion))
        .thenReturn(flareReportPeriod);

    YearMonth startYearMonth = flareReportPeriod.getReportStartYearMonth();

    FlareReportForm flareReportForm = flareReportService.getFlareReportForm(applicationVersion);

    assertThat(flareReportForm)
        .extracting(FlareReportForm::getStartYear,
            FlareReportForm::getEndYear)
        .containsExactly(String.valueOf(startYearMonth.getYear()),
            String.valueOf(endYearMonth.getYear()));

    assertThat(flareReportForm.getFlareReportMonthForms())
        .extracting(FlareReportMonthForm::getYear,
            FlareReportMonthForm::getMonth,
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
  void saveFlareReport() {
    flareReportService.saveFlareReport(applicationVersion, FlareReportTestUtil.getFullFlareReportFormForYear(2022));

    verify(flareReportMonthRepository, times(1)).deleteAllByApplicationVersion(applicationVersion);

    ArgumentCaptor<FlareReportMonth> flareReportMonthArgumentCaptor = ArgumentCaptor.forClass(FlareReportMonth.class);
    verify(flareReportMonthRepository, times(12)).save(flareReportMonthArgumentCaptor.capture());
  }

  @Test
  void getFlareReportSummaryCards_noPeriodExists() {
    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(flareReportService.getFlareReportSummaryCards(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCardList());
  }

  @Test
  void getFlareReportSummaryCards_periodExists_noReportMonths() {
    var flareReportPeriod = FlareReportTestUtil.getFullFlareReportPeriod();
    var simpleSummaryCard = SummaryTestUtil.getSimpleSummaryCard();

    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion))
        .thenReturn(Optional.of(flareReportPeriod));
    when(emissionReportSummaryService.getReportPeriodSummaryCard(flareReportPeriod, ApplicationType.FLARE))
        .thenReturn(simpleSummaryCard);
    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(flareReportService.getFlareReportSummaryCards(applicationVersion))
        .isEqualTo(List.of(simpleSummaryCard));
  }

  @Test
  void getFlareReportSummaryCards_periodExists_reportMonthsExist() {
    var flareReportPeriod = FlareReportTestUtil.getFullFlareReportPeriod();
    var flareReportMonths = FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, flareReportPeriod.getReportEndYear());
    var simpleSummaryCard = SummaryTestUtil.getSimpleSummaryCard();
    var tableSummaryCard = SummaryTestUtil.getTableSummaryCard();
    var flareCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var flareAverageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion))
        .thenReturn(Optional.of(flareReportPeriod));
    when(emissionReportSummaryService.getReportPeriodSummaryCard(flareReportPeriod, ApplicationType.FLARE))
        .thenReturn(simpleSummaryCard);
    when(flareReportMonthRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(flareReportMonths);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion))
        .thenReturn(flareCategoryUnit);
    when(applicationUnitService.getFlareAverageUnit(applicationVersion))
        .thenReturn(flareAverageUnit);
    when(emissionReportSummaryService.getReportTableSummaryCard(flareReportMonths, flareCategoryUnit, flareAverageUnit))
        .thenReturn(tableSummaryCard);

    assertThat(flareReportService.getFlareReportSummaryCards(applicationVersion))
        .isEqualTo(List.of(simpleSummaryCard, tableSummaryCard));
  }
}
