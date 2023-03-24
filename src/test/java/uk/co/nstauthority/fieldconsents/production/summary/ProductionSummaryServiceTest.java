package uk.co.nstauthority.fieldconsents.production.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;
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

  @Mock
  private ShortTermProductionService shortTermProductionService;

  @Mock
  private AnnualProductionService annualProductionService;

  @Mock
  private LongTermProductionService longTermProductionService;

  @Mock
  private ApplicationUnitService applicationUnitService;

  @InjectMocks
  private ProductionSummaryService productionSummaryService;

  private ApplicationVersion applicationVersion;

  private final ProductionUnit oilUnit = ProductionUnit.KSCM_PER_MONTH;

  private final ProductionUnit gasUnit = ProductionUnit.KSCM_PER_MONTH;

  private final ProductionUnit averageUnit = ProductionUnit.KSCM_PER_DAY;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
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
    var productionMonths = ProductionTestUtils.getShortTermProductionMonthsData(applicationVersion);

    when(shortTermProductionService.getShortTermProductionMonths(applicationVersion))
        .thenReturn(productionMonths);
    when(applicationUnitService.getProductionOilUnit(applicationVersion))
        .thenReturn(oilUnit);
    when(applicationUnitService.getProductionGasUnit(applicationVersion))
        .thenReturn(gasUnit);
    when(applicationUnitService.getProductionAverageUnit(applicationVersion))
        .thenReturn(averageUnit);

    var totalDays = productionMonths.stream()
        .mapToInt(productionMonth ->
            DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate())
        )
        .sum();
    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    assertThat(productionSummaryService.getShortTermConsentSummaryCard(applicationVersion))
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
                            bigDecimalToFormattedString(BigDecimalUtil.divideRound(oilMinTotal, totalDays)),
                            bigDecimalToFormattedString(BigDecimalUtil.divideRound(oilMaxTotal, totalDays)),
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
    var productionMonths = ProductionTestUtils.getAnnualProductionMonthsData(applicationVersion);

    when(annualProductionService.getAnnualProductionMonths(applicationVersion))
        .thenReturn(productionMonths);
    when(applicationUnitService.getProductionOilUnit(applicationVersion))
        .thenReturn(oilUnit);
    when(applicationUnitService.getProductionGasUnit(applicationVersion))
        .thenReturn(gasUnit);
    when(applicationUnitService.getProductionAverageUnit(applicationVersion))
        .thenReturn(averageUnit);

    var totalDays = productionMonths.stream()
        .mapToInt(productionMonth ->
            YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()).lengthOfMonth()
        )
        .sum();
    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    assertThat(productionSummaryService.getAnnualConsentSummaryCard(applicationVersion))
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
                            bigDecimalToFormattedString(BigDecimalUtil.divideRound(oilMinTotal, totalDays)),
                            bigDecimalToFormattedString(BigDecimalUtil.divideRound(oilMaxTotal, totalDays)),
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
                        getSummaryTableRowForLongTermMonth(productionYears.get(0)),
                        getSummaryTableRowForLongTermMonth(productionYears.get(1)),
                        getSummaryTableRowForLongTermMonth(productionYears.get(2)),
                        getSummaryTableRowForLongTermMonth(productionYears.get(3)),
                        getSummaryTableRowForLongTermMonth(productionYears.get(4))
                    )
                )
            )
        );
  }

  private SummaryTableRow getSummaryTableRowForLongTermMonth(LongTermProductionYear productionYear) {
    return new SummaryTableRow(List.of(
        String.valueOf(productionYear.getYear()),
        bigDecimalToFormattedString(productionYear.getOilMinValue()),
        bigDecimalToFormattedString(productionYear.getOilMaxValue()),
        bigDecimalToFormattedString(productionYear.getGasMinValue()),
        bigDecimalToFormattedString(productionYear.getGasMaxValue())
    ));
  }
}