package uk.co.nstauthority.fieldconsents.production;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;
import uk.co.nstauthority.fieldconsents.util.BigDecimalUtil;

public record ProductionView(
    String periodHeading,
    String consentDaysHeading,
    String oilMinHeading,
    String oilMaxHeading,
    String gasMinHeading,
    String gasMaxHeading,
    List<ProductionRowView> productionRows
) {
  private static final String MONTH_HEADING = "Month";
  private static final String YEAR_HEADING = "Year";
  private static final String CONSENT_DAYS_HEADING = "Consent days";
  private static final String MIN_OIL_HEADING = "Minimum oil (%s)";
  private static final String MAX_OIL_HEADING = "Maximum oil (%s)";
  private static final String MIN_GAS_HEADING = "Minimum gas (%s)";
  private static final String MAX_GAS_HEADING = "Maximum gas (%s)";
  private static final String TOTAL_PROMPT = "Totals";
  private static final String AVERAGE_PROMPT = "Daily average (%s)";

  public static ProductionView empty() {
    return new ProductionView(null, null, null, null, null, null, Collections.emptyList());
  }

  public static ProductionView fromShortTerm(List<ShortTermProductionMonth> productionMonths,
                                             ProductionUnit oilUnit,
                                             ProductionUnit gasUnit,
                                             ProductionUnit averageUnit) {

    if (productionMonths.isEmpty()) {
      return empty();
    }

    List<ProductionRowView> productionRowViews = new ArrayList<>();
    int totalDays = 0;
    for (ShortTermProductionMonth productionMonth : productionMonths) {
      var productionRowView = ProductionRowView.fromShortTerm(productionMonth);
      productionRowViews.add(productionRowView);
      totalDays = totalDays + Integer.parseInt(productionRowView.consentDays());
    }

    return addTotalsAndAverages(productionMonths, productionRowViews, totalDays, oilUnit, gasUnit, averageUnit);
  }

  public static ProductionView fromAnnual(List<AnnualProductionMonth> productionMonths,
                                          ProductionUnit oilUnit,
                                          ProductionUnit gasUnit,
                                          ProductionUnit averageUnit) {
    if (productionMonths.isEmpty()) {
      return empty();
    }

    List<ProductionRowView> productionRowViews = new ArrayList<>();
    int totalDays = 0;
    for (AnnualProductionMonth productionMonth : productionMonths) {
      var productionRowView = ProductionRowView.fromAnnual(productionMonth);
      productionRowViews.add(productionRowView);
      totalDays = totalDays + Integer.parseInt(productionRowView.consentDays());
    }

    return addTotalsAndAverages(productionMonths, productionRowViews, totalDays, oilUnit, gasUnit, averageUnit);
  }

  private static ProductionView addTotalsAndAverages(List<? extends ProductionRow> productionMonths,
                                                     List<ProductionRowView> productionRowViews,
                                                     int totalDays,
                                                     ProductionUnit oilUnit,
                                                     ProductionUnit gasUnit,
                                                     ProductionUnit averageUnit) {

    var oilMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMinValue);
    var oilMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getOilMaxValue);
    var gasMinTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMinValue);
    var gasMaxTotal = BigDecimalUtil.sum(productionMonths, ProductionRow::getGasMaxValue);

    productionRowViews.add(ProductionRowView.from(TOTAL_PROMPT,
        totalDays, oilMinTotal, oilMaxTotal, gasMinTotal, gasMaxTotal));

    productionRowViews.add(ProductionRowView.from(
        AVERAGE_PROMPT.formatted(averageUnit.getDisplayName()),
        null,
        BigDecimalUtil.divideRound(oilMinTotal, totalDays),
        BigDecimalUtil.divideRound(oilMaxTotal, totalDays),
        BigDecimalUtil.divideRound(gasMinTotal, totalDays),
        BigDecimalUtil.divideRound(gasMaxTotal, totalDays)
    ));

    return new ProductionView(
        MONTH_HEADING,
        CONSENT_DAYS_HEADING,
        MIN_OIL_HEADING.formatted(oilUnit.getDisplayName()),
        MAX_OIL_HEADING.formatted(oilUnit.getDisplayName()),
        MIN_GAS_HEADING.formatted(gasUnit.getDisplayName()),
        MAX_GAS_HEADING.formatted(gasUnit.getDisplayName()),
        productionRowViews
    );
  }

  public static ProductionView fromLongTerm(List<LongTermProductionYear> productionYears,
                                            ProductionUnit oilUnit,
                                            ProductionUnit gasUnit) {
    if (productionYears.isEmpty()) {
      return empty();
    }

    List<ProductionRowView> productionRowViews = new ArrayList<>();
    for (LongTermProductionYear productionYear : productionYears) {
      productionRowViews.add(ProductionRowView.fromLongTerm(productionYear));
    }

    return new ProductionView(
        YEAR_HEADING,
        CONSENT_DAYS_HEADING,
        MIN_OIL_HEADING.formatted(oilUnit.getDisplayName()),
        MAX_OIL_HEADING.formatted(oilUnit.getDisplayName()),
        MIN_GAS_HEADING.formatted(gasUnit.getDisplayName()),
        MAX_GAS_HEADING.formatted(gasUnit.getDisplayName()),
        productionRowViews
    );
  }
}
