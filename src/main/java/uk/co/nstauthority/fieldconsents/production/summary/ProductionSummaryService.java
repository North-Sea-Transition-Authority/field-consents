package uk.co.nstauthority.fieldconsents.production.summary;

import java.time.YearMonth;
import java.util.function.UnaryOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionService;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionService;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

@Service
public class ProductionSummaryService {

  static final String MONTH_HEADING = "Month";
  static final String YEAR_HEADING = "Year";
  static final String CONSENT_DAYS_HEADING = "Consent days";
  static final UnaryOperator<String> MIN_OIL_HEADING = "Minimum oil (%s)"::formatted;
  static final UnaryOperator<String> MAX_OIL_HEADING = "Maximum oil (%s)"::formatted;
  static final UnaryOperator<String> MIN_GAS_HEADING = "Minimum gas (%s)"::formatted;
  static final UnaryOperator<String> MAX_GAS_HEADING = "Maximum gas (%s)"::formatted;
  static final String TOTAL_PROMPT = "Totals";
  static final UnaryOperator<String> AVERAGE_PROMPT = "Daily average (%s)"::formatted;

  private final ShortTermProductionService shortTermProductionService;

  private final AnnualProductionService annualProductionService;

  private final LongTermProductionService longTermProductionService;

  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public ProductionSummaryService(ShortTermProductionService shortTermProductionService,
                                  AnnualProductionService annualProductionService,
                                  LongTermProductionService longTermProductionService,
                                  ApplicationUnitService applicationUnitService) {
    this.shortTermProductionService = shortTermProductionService;
    this.annualProductionService = annualProductionService;
    this.longTermProductionService = longTermProductionService;
    this.applicationUnitService = applicationUnitService;
  }

  public SummaryCard getShortTermConsentSummaryCard(ApplicationVersion applicationVersion) {

    var productionMonths = shortTermProductionService.getShortTermProductionMonths(applicationVersion);

    if (productionMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var oilUnit = applicationUnitService.getProductionOilUnit(applicationVersion);
    var gasUnit = applicationUnitService.getProductionGasUnit(applicationVersion);
    var averageUnit = applicationUnitService.getProductionAverageUnit(applicationVersion);
    var oilAverageConversionFactor = applicationUnitService.getProductionAverageConversionFactor(oilUnit, averageUnit);

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        CONSENT_DAYS_HEADING,
        MIN_OIL_HEADING.apply(oilUnit.getDisplayName()),
        MAX_OIL_HEADING.apply(oilUnit.getDisplayName()),
        MIN_GAS_HEADING.apply(gasUnit.getDisplayName()),
        MAX_GAS_HEADING.apply(gasUnit.getDisplayName())
    );

    int totalDays = 0;
    for (var productionMonth : productionMonths) {
      var consentDays = DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate());
      summaryTable.addRow(
          DateUtils.formatShort(productionMonth.getMonth(), productionMonth.getYear()),
          consentDays,
          productionMonth.getOilMinValue(),
          productionMonth.getOilMaxValue(),
          productionMonth.getGasMinValue(),
          productionMonth.getGasMaxValue()
      );
      totalDays = totalDays + consentDays;
    }

    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            totalDays,
            oilMinTotal,
            oilMaxTotal,
            gasMinTotal,
            gasMaxTotal
        )
        .addRow(
            AVERAGE_PROMPT.apply(averageUnit.getDisplayName()),
            null,
            BigDecimalUtil.divideRound(oilMinTotal, totalDays * oilAverageConversionFactor),
            BigDecimalUtil.divideRound(oilMaxTotal, totalDays * oilAverageConversionFactor),
            BigDecimalUtil.divideRound(gasMinTotal, totalDays),
            BigDecimalUtil.divideRound(gasMaxTotal, totalDays)
        );

    return SummaryCard.tableSummaryCard(summaryTable);
  }

  public SummaryCard getAnnualConsentSummaryCard(ApplicationVersion applicationVersion) {

    var productionMonths = annualProductionService.getAnnualProductionMonths(applicationVersion);

    if (productionMonths.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var oilUnit = applicationUnitService.getProductionOilUnit(applicationVersion);
    var gasUnit = applicationUnitService.getProductionGasUnit(applicationVersion);
    var averageUnit = applicationUnitService.getProductionAverageUnit(applicationVersion);
    var oilAverageConversionFactor = applicationUnitService.getProductionAverageConversionFactor(oilUnit, averageUnit);

    var summaryTable = SummaryTableView.newWithHeading(
        MONTH_HEADING,
        MIN_OIL_HEADING.apply(oilUnit.getDisplayName()),
        MAX_OIL_HEADING.apply(oilUnit.getDisplayName()),
        MIN_GAS_HEADING.apply(gasUnit.getDisplayName()),
        MAX_GAS_HEADING.apply(gasUnit.getDisplayName())
    );

    int totalDays = 0;
    for (var productionMonth : productionMonths) {
      summaryTable.addRow(
          DateUtils.formatShort(productionMonth.getMonth(), productionMonth.getYear()),
          productionMonth.getOilMinValue(),
          productionMonth.getOilMaxValue(),
          productionMonth.getGasMinValue(),
          productionMonth.getGasMaxValue()
      );
      totalDays = totalDays + YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()).lengthOfMonth();
    }

    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    summaryTable
        .addRow(
            TOTAL_PROMPT,
            oilMinTotal,
            oilMaxTotal,
            gasMinTotal,
            gasMaxTotal
        )
        .addRow(
            AVERAGE_PROMPT.apply(averageUnit.getDisplayName()),
            BigDecimalUtil.divideRound(oilMinTotal, totalDays * oilAverageConversionFactor),
            BigDecimalUtil.divideRound(oilMaxTotal, totalDays * oilAverageConversionFactor),
            BigDecimalUtil.divideRound(gasMinTotal, totalDays),
            BigDecimalUtil.divideRound(gasMaxTotal, totalDays)
        );

    return SummaryCard.tableSummaryCard(summaryTable);
  }

  public SummaryCard getLongTermConsentSummaryCard(ApplicationVersion applicationVersion) {

    var productionYears = longTermProductionService.getLongTermProductionYears(applicationVersion);

    if (productionYears.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var oilUnit = applicationUnitService.getProductionOilUnit(applicationVersion);
    var gasUnit = applicationUnitService.getProductionGasUnit(applicationVersion);

    var summaryTable = SummaryTableView.newWithHeading(
        YEAR_HEADING,
        MIN_OIL_HEADING.apply(oilUnit.getDisplayName()),
        MAX_OIL_HEADING.apply(oilUnit.getDisplayName()),
        MIN_GAS_HEADING.apply(gasUnit.getDisplayName()),
        MAX_GAS_HEADING.apply(gasUnit.getDisplayName())
    );

    for (var productionYear : productionYears) {
      summaryTable.addRow(
          productionYear.getYear(),
          productionYear.getOilMinValue(),
          productionYear.getOilMaxValue(),
          productionYear.getGasMinValue(),
          productionYear.getGasMaxValue()
      );
    }

    return SummaryCard.tableSummaryCard(summaryTable);
  }
}
