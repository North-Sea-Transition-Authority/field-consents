package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

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
class VentReportSummaryServiceTest {

  @Mock
  private VentReportService ventReportService;

  @Mock
  private VentReportPeriodService ventReportPeriodService;

  @Mock
  private EmissionReportSummaryService emissionReportSummaryService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private VentReportSummaryService ventReportSummaryService;

  private ApplicationVersion applicationVersion;

  private final EmissionsChartData emissionsChartData = new EmissionsChartData(
      "Example chart title",
      List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"),
      "Month",
      "Days in month",
      List.of(new EmissionsChartData.Series(
          "Days",
          "highcharts-colour-blue",
          List.of(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
      ))
  );

  @BeforeEach
  void setUp() {
    applicationVersion = VentReportTestUtil.ventAppVersion;
  }

  @Test
  void getVentReportSummaryCards_noPeriodExists() {
    when(ventReportPeriodService.findVentReportPeriod(applicationVersion))
        .thenReturn(Optional.empty());

    assertThat(ventReportSummaryService.getVentReportSummaryCards(applicationVersion))
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
    when(ventReportService.getVentReportMonths(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(ventReportSummaryService.getVentReportSummaryCards(applicationVersion))
        .isEqualTo(List.of(simpleSummaryCard));
  }

  @Test
  void getVentReportSummaryCards_periodExists_reportMonthsExist() {
    var ventReportPeriod = VentReportTestUtil.getFullVentReportPeriod();
    var ventReportMonths = VentReportTestUtil.getVentReportMonthsForYear(applicationVersion, ventReportPeriod.getReportEndYear());
    var simpleSummaryCard = SummaryTestUtil.getSimpleSummaryCard();
    var stackedBarChartSummaryCard = SummaryTestUtil.getStackedBarChartSummaryCard(emissionsChartData);
    var tableSummaryCard = SummaryTestUtil.getTableSummaryCard();
    var ventCategoryUnit = FlareVentUnit.TONNES_PER_MONTH;
    var ventAverageUnit = FlareVentUnit.TONNES_PER_DAY;

    when(ventReportPeriodService.findVentReportPeriod(applicationVersion))
        .thenReturn(Optional.of(ventReportPeriod));
    when(emissionReportSummaryService.getReportPeriodSummaryCard(ventReportPeriod, ApplicationType.VENT))
        .thenReturn(simpleSummaryCard);
    when(ventReportService.getVentReportMonths(applicationVersion))
        .thenReturn(ventReportMonths);
    when(applicationUnitService.getVentCategoryUnit(applicationVersion))
        .thenReturn(ventCategoryUnit);
    when(applicationUnitService.getVentAverageUnit(applicationVersion))
        .thenReturn(ventAverageUnit);
    when(emissionReportSummaryService.getEmissionsReportChartSummaryCard(applicationVersion))
        .thenReturn(Optional.of(stackedBarChartSummaryCard));
    when(emissionReportSummaryService.getReportTableSummaryCard(ventReportMonths, ventCategoryUnit, ventAverageUnit))
        .thenReturn(tableSummaryCard);

    assertThat(ventReportSummaryService.getVentReportSummaryCards(applicationVersion))
        .isEqualTo(List.of(simpleSummaryCard, stackedBarChartSummaryCard, tableSummaryCard));
  }

}