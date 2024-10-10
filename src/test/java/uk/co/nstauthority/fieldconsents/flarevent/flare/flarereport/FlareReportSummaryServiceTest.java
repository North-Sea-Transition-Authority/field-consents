package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.charts.EmissionsChartData;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentUnit;
import uk.co.nstauthority.fieldconsents.flarevent.summary.EmissionReportSummaryService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTestUtil;

@ExtendWith(MockitoExtension.class)
class FlareReportSummaryServiceTest {

  @Mock
  private FlareReportService flareReportService;

  @Mock
  private FlareReportPeriodService flareReportPeriodService;

  @Mock
  private EmissionReportSummaryService emissionReportSummaryService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private FlareReportSummaryService flareReportSummaryService;

  private ApplicationVersion applicationVersion;

  private final EmissionsChartData emissionsChartData = new EmissionsChartData(
      "Example chart title",
      List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"),
      "Month",
      "Days in month",
      List.of(new EmissionsChartData.Series(
          "Days",
          "highcharts-color-1",
          List.of(
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 29),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 30),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 30),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 30),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31),
              new EmissionsChartData.DataPoint("highcharts-color-1", 30),
              new EmissionsChartData.DataPoint("highcharts-color-1", 31))
          ))
  );

  @BeforeEach
  void setUp() {
    applicationVersion = FlareReportTestUtil.flareAppVersion;
  }

  @Test
  void getFlareReportSummaryCards_noPeriodExists() {
    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(flareReportSummaryService.getFlareReportSummaryCards(applicationVersion))
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
    when(flareReportService.getFlareReportMonths(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(flareReportSummaryService.getFlareReportSummaryCards(applicationVersion))
        .isEqualTo(List.of(simpleSummaryCard));
  }

  @Test
  void getFlareReportSummaryCards_periodExists_reportMonthsExist() {
    var flareReportPeriod = FlareReportTestUtil.getFullFlareReportPeriod();
    var flareReportMonths = FlareReportTestUtil.getFlareReportMonthsForYear(applicationVersion, flareReportPeriod.getReportEndYear());
    var simpleSummaryCard = SummaryTestUtil.getSimpleSummaryCard();
    var tableSummaryCard = SummaryTestUtil.getTableSummaryCard();
    var stackedBarChartSummaryCard = SummaryTestUtil.getStackedBarChartSummaryCard(emissionsChartData);
    var flareCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var flareAverageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(flareReportPeriodService.findFlareReportPeriod(applicationVersion))
        .thenReturn(Optional.of(flareReportPeriod));
    when(emissionReportSummaryService.getReportPeriodSummaryCard(flareReportPeriod, ApplicationType.FLARE))
        .thenReturn(simpleSummaryCard);
    when(flareReportService.getFlareReportMonths(applicationVersion))
        .thenReturn(flareReportMonths);
    when(applicationUnitService.getFlareCategoryUnit(applicationVersion))
        .thenReturn(flareCategoryUnit);
    when(applicationUnitService.getFlareAverageUnit(applicationVersion))
        .thenReturn(flareAverageUnit);
    when(emissionReportSummaryService.getReportTableSummaryCard(flareReportMonths, flareCategoryUnit, flareAverageUnit))
        .thenReturn(tableSummaryCard);
    when(emissionReportSummaryService.getEmissionsReportChartSummaryCard(applicationVersion))
        .thenReturn(Optional.of(stackedBarChartSummaryCard));

    assertThat(flareReportSummaryService.getFlareReportSummaryCards(applicationVersion))
        .isEqualTo(List.of(simpleSummaryCard, stackedBarChartSummaryCard, tableSummaryCard));
  }

}
