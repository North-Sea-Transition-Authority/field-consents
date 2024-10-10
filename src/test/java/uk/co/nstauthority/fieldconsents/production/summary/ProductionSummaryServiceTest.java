package uk.co.nstauthority.fieldconsents.production.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;
import static uk.co.nstauthority.fieldconsents.production.ProductionUnit.KSCM_PER_DAY;
import static uk.co.nstauthority.fieldconsents.production.ProductionUnit.KSCM_PER_MONTH;
import static uk.co.nstauthority.fieldconsents.production.ProductionUnit.SCM_PER_MONTH;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.AVERAGE_PROMPT;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.CONSENT_DAYS_HEADING;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.MAX_GAS_HEADING;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.MAX_OIL_HEADING;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.MIN_GAS_HEADING;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.MIN_OIL_HEADING;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.MONTH_HEADING;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.TOTAL_PROMPT;
import static uk.co.nstauthority.fieldconsents.production.summary.ProductionSummaryService.YEAR_HEADING;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.charts.ProductionChartData;
import uk.co.nstauthority.fieldconsents.charts.ProductionChartDataService;
import uk.co.nstauthority.fieldconsents.charts.ProductionType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.ProductionTestUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionUnit;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryCardType;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@ExtendWith(MockitoExtension.class)
class ProductionSummaryServiceTest {

  private final ProductionChartData oilProductionChartData = new ProductionChartData(
      "Example chart title",
      List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"),
      "Month",
      "Days in month",
      List.of(new ProductionChartData.Series(
              "Days",
              "highcharts-color-1",
              "columnrange",
              List.of(
                  new ProductionChartData.DataPoint(
                      0,
                      1,
                      5,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      1,
                      2,
                      10,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      2,
                      3,
                      15,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      3,
                      4,
                      20,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      4,
                      5,
                      25,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      5,
                      6,
                      30,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      6,
                      7,
                      35,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      7,
                      8,
                      40,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      8,
                      9,
                      45,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      9,
                      10,
                      50,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      10,
                      11,
                      55,
                      "highcharts-color-1"),
                  new ProductionChartData.DataPoint(
                      11,
                      12,
                      60,
                      "highcharts-color-1")
              )
          )
      )
  );

  private final ProductionChartData gasProductionChartData = new ProductionChartData(
      "Example chart title2",
      List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"),
      "Month2",
      "Days in month2",
      List.of(new ProductionChartData.Series(
              "Days",
              "highcharts-color-2",
              "columnrange",
              List.of(
                  new ProductionChartData.DataPoint(
                      0,
                      1,
                      5,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      1,
                      2,
                      10,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      2,
                      3,
                      15,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      3,
                      4,
                      20,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      4,
                      5,
                      25,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      5,
                      6,
                      30,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      6,
                      7,
                      35,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      7,
                      8,
                      40,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      8,
                      9,
                      45,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      9,
                      10,
                      50,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      10,
                      11,
                      55,
                      "highcharts-color-2"),
                  new ProductionChartData.DataPoint(
                      11,
                      12,
                      60,
                      "highcharts-color-2")
              )
          )
      )
  );

  @Mock
  private ShortTermProductionService shortTermProductionService;

  @Mock
  private AnnualProductionService annualProductionService;

  @Mock
  private LongTermProductionService longTermProductionService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @Mock
  private ProductionChartDataService productionChartDataService;

  @InjectMocks
  private ProductionSummaryService productionSummaryService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getShortTermConsentSummaryCard_noMonthsData() {
    when(shortTermProductionService.getShortTermProductionMonths(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(productionSummaryService.getShortTermConsentSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getShortTermConsentSummaryCard() {
    var productionUnit = SCM_PER_MONTH;
    var averageProductionUnit = KSCM_PER_DAY;
    var productionAverageConversionFactor = 1000;
    var productionMonths = ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion);

    when(shortTermProductionService.getShortTermProductionMonths(applicationVersion))
        .thenReturn(productionMonths);
    when(applicationUnitService.getProductionOilUnit(applicationVersion))
        .thenReturn(productionUnit);
    when(applicationUnitService.getProductionGasUnit(applicationVersion))
        .thenReturn(productionUnit);
    when(applicationUnitService.getProductionAverageUnit(applicationVersion))
        .thenReturn(averageProductionUnit);
    when(applicationUnitService.getProductionAverageConversionFactor(productionUnit, averageProductionUnit))
        .thenReturn(productionAverageConversionFactor);

    var shortTermConsentSummaryCard = productionSummaryService.getShortTermConsentSummaryCard(applicationVersion);

    assertShortTermConsentSummaryCard(
        productionMonths, shortTermConsentSummaryCard,
        productionUnit, productionUnit, averageProductionUnit,
        productionAverageConversionFactor
    );
  }

  private void assertShortTermConsentSummaryCard(List<ShortTermProductionMonth> productionMonths,
                                                 SummaryCard shortTermConsentSummaryCard,
                                                 ProductionUnit oilUnit,
                                                 ProductionUnit gasUnit,
                                                 ProductionUnit averageUnit,
                                                 int oilAverageConversionFactor) {

    var totalDays = productionMonths.stream()
        .mapToInt(productionMonth ->
            DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate())
        )
        .sum();
    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    assertThat(shortTermConsentSummaryCard)
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        new SummaryTableRow(List.of(
                            MONTH_HEADING,
                            CONSENT_DAYS_HEADING,
                            MIN_OIL_HEADING.apply(oilUnit.getDisplayName()),
                            MAX_OIL_HEADING.apply(oilUnit.getDisplayName()),
                            MIN_GAS_HEADING.apply(gasUnit.getDisplayName()),
                            MAX_GAS_HEADING.apply(gasUnit.getDisplayName())
                        )),
                        getSummaryTableRowForShortTermMonth(productionMonths.get(0)),
                        getSummaryTableRowForShortTermMonth(productionMonths.get(1)),
                        getSummaryTableRowForShortTermMonth(productionMonths.get(2)),
                        getSummaryTableRowForShortTermMonth(productionMonths.get(3)),
                        getSummaryTableRowForShortTermMonth(productionMonths.get(4)),
                        getSummaryTableRowForShortTermMonth(productionMonths.get(5)),
                        getSummaryTableRowForShortTermMonth(productionMonths.get(6)),
                        new SummaryTableRow(List.of(
                            TOTAL_PROMPT,
                            String.valueOf(totalDays),
                            bigDecimalToFormattedString(oilMinTotal),
                            bigDecimalToFormattedString(oilMaxTotal),
                            bigDecimalToFormattedString(gasMinTotal),
                            bigDecimalToFormattedString(gasMaxTotal)
                        )),
                        new SummaryTableRow(Stream.of(
                            AVERAGE_PROMPT.apply(averageUnit.getDisplayName()),
                            null,
                            bigDecimalToFormattedString(
                                BigDecimalUtil.divideRound(oilMinTotal, totalDays * oilAverageConversionFactor)),
                            bigDecimalToFormattedString(
                                BigDecimalUtil.divideRound(oilMaxTotal, totalDays * oilAverageConversionFactor)),
                            bigDecimalToFormattedString(BigDecimalUtil.divideRound(gasMinTotal, totalDays)),
                            bigDecimalToFormattedString(BigDecimalUtil.divideRound(gasMaxTotal, totalDays))
                        ).toList())
                    )
                )
            )
        );
  }

  private SummaryTableRow getSummaryTableRowForShortTermMonth(ShortTermProductionMonth productionMonth) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(productionMonth.getMonth(), productionMonth.getYear()),
        String.valueOf(DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate())),
        bigDecimalToFormattedString(productionMonth.getOilMinValue()),
        bigDecimalToFormattedString(productionMonth.getOilMaxValue()),
        bigDecimalToFormattedString(productionMonth.getGasMinValue()),
        bigDecimalToFormattedString(productionMonth.getGasMaxValue())
    ));
  }

  @Test
  void getAnnualConsentSummaryCard_noMonthsData() {
    when(annualProductionService.getAnnualProductionMonths(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(productionSummaryService.getAnnualConsentSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getAnnualConsentSummaryCard() {
    var productionUnit = SCM_PER_MONTH;
    var averageProductionUnit = KSCM_PER_DAY;
    var productionAverageConversionFactor = 1000;
    var productionMonths = ProductionTestUtils.getAnnualProductionMonthsData(applicationVersion);

    when(annualProductionService.getAnnualProductionMonths(applicationVersion))
        .thenReturn(productionMonths);
    when(applicationUnitService.getProductionOilUnit(applicationVersion))
        .thenReturn(productionUnit);
    when(applicationUnitService.getProductionGasUnit(applicationVersion))
        .thenReturn(productionUnit);
    when(applicationUnitService.getProductionAverageUnit(applicationVersion))
        .thenReturn(averageProductionUnit);
    when(applicationUnitService.getProductionAverageConversionFactor(productionUnit, averageProductionUnit))
        .thenReturn(productionAverageConversionFactor);

    var annualConsentSummaryCard = productionSummaryService.getAnnualConsentSummaryCard(applicationVersion);

    assertAnnualConsentSummaryCard(
        productionMonths, annualConsentSummaryCard,
        productionUnit, productionUnit, averageProductionUnit,
        productionAverageConversionFactor
    );
  }

  private void assertAnnualConsentSummaryCard(List<AnnualProductionMonth> productionMonths,
                                              SummaryCard annualConsentSummaryCard,
                                              ProductionUnit oilUnit,
                                              ProductionUnit gasUnit,
                                              ProductionUnit averageUnit,
                                              int oilAverageConversionFactor) {
    var totalDays = productionMonths.stream()
        .mapToInt(productionMonth ->
            YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()).lengthOfMonth()
        )
        .sum();
    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    assertThat(annualConsentSummaryCard)
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        new SummaryTableRow(List.of(
                            MONTH_HEADING,
                            MIN_OIL_HEADING.apply(oilUnit.getDisplayName()),
                            MAX_OIL_HEADING.apply(oilUnit.getDisplayName()),
                            MIN_GAS_HEADING.apply(gasUnit.getDisplayName()),
                            MAX_GAS_HEADING.apply(gasUnit.getDisplayName())
                        )),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(0)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(1)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(2)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(3)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(4)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(5)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(6)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(7)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(8)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(9)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(10)),
                        getSummaryTableRowForAnnualMonth(productionMonths.get(11)),
                        new SummaryTableRow(List.of(
                            TOTAL_PROMPT,
                            bigDecimalToFormattedString(oilMinTotal),
                            bigDecimalToFormattedString(oilMaxTotal),
                            bigDecimalToFormattedString(gasMinTotal),
                            bigDecimalToFormattedString(gasMaxTotal)
                        )),
                        new SummaryTableRow(Stream.of(
                            AVERAGE_PROMPT.apply(averageUnit.getDisplayName()),
                            bigDecimalToFormattedString(
                                BigDecimalUtil.divideRound(oilMinTotal, totalDays * oilAverageConversionFactor)),
                            bigDecimalToFormattedString(
                                BigDecimalUtil.divideRound(oilMaxTotal, totalDays * oilAverageConversionFactor)),
                            bigDecimalToFormattedString(BigDecimalUtil.divideRound(gasMinTotal, totalDays)),
                            bigDecimalToFormattedString(BigDecimalUtil.divideRound(gasMaxTotal, totalDays))
                        ).toList())
                    )
                )
            )
        );
  }

  private SummaryTableRow getSummaryTableRowForAnnualMonth(AnnualProductionMonth productionMonth) {
    return new SummaryTableRow(List.of(
        DateUtils.formatShort(productionMonth.getMonth(), productionMonth.getYear()),
        bigDecimalToFormattedString(productionMonth.getOilMinValue()),
        bigDecimalToFormattedString(productionMonth.getOilMaxValue()),
        bigDecimalToFormattedString(productionMonth.getGasMinValue()),
        bigDecimalToFormattedString(productionMonth.getGasMaxValue())
    ));
  }

  @Test
  void getLongTermConsentSummaryCard_noYearsData() {
    when(longTermProductionService.getLongTermProductionYears(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThat(productionSummaryService.getLongTermConsentSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }

  @Test
  void getLongTermConsentSummaryCard() {
    var oilUnit = KSCM_PER_MONTH;
    var gasUnit = KSCM_PER_MONTH;

    var productionYears = ProductionTestUtils.getLongTermProductionYearsData(applicationVersion);

    when(longTermProductionService.getLongTermProductionYears(applicationVersion))
        .thenReturn(productionYears);
    when(applicationUnitService.getProductionOilUnit(applicationVersion))
        .thenReturn(oilUnit);
    when(applicationUnitService.getProductionGasUnit(applicationVersion))
        .thenReturn(gasUnit);

    assertThat(productionSummaryService.getLongTermConsentSummaryCard(applicationVersion))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.TABLE_SUMMARY,
                new SummaryTableView(
                    List.of(
                        new SummaryTableRow(List.of(
                            YEAR_HEADING,
                            MIN_OIL_HEADING.apply(oilUnit.getDisplayName()),
                            MAX_OIL_HEADING.apply(oilUnit.getDisplayName()),
                            MIN_GAS_HEADING.apply(gasUnit.getDisplayName()),
                            MAX_GAS_HEADING.apply(gasUnit.getDisplayName())
                        )),
                        getSummaryTableRowForLongTermYear(productionYears.get(0)),
                        getSummaryTableRowForLongTermYear(productionYears.get(1)),
                        getSummaryTableRowForLongTermYear(productionYears.get(2)),
                        getSummaryTableRowForLongTermYear(productionYears.get(3)),
                        getSummaryTableRowForLongTermYear(productionYears.get(4))
                    )
                )
            )
        );
  }

  private SummaryTableRow getSummaryTableRowForLongTermYear(LongTermProductionYear productionYear) {
    return new SummaryTableRow(List.of(
        String.valueOf(productionYear.getYear()),
        bigDecimalToFormattedString(productionYear.getOilMinValue()),
        bigDecimalToFormattedString(productionYear.getOilMaxValue()),
        bigDecimalToFormattedString(productionYear.getGasMinValue()),
        bigDecimalToFormattedString(productionYear.getGasMaxValue())
    ));
  }

  @Test
  void getProductionConsentChartSummaryCards() {
    when(productionChartDataService.getProductionChartData(applicationVersion, ProductionType.OIL))
        .thenReturn(Optional.of(oilProductionChartData));
    when(productionChartDataService.getProductionChartData(applicationVersion, ProductionType.GAS))
        .thenReturn(Optional.of(gasProductionChartData));

    assertThat(productionSummaryService.getProductionConsentChartSummaryCards(applicationVersion))
        .contains(
            SummaryCard.floatingBarChartSummaryCard(oilProductionChartData),
            SummaryCard.floatingBarChartSummaryCard(gasProductionChartData)
        );
  }
}
