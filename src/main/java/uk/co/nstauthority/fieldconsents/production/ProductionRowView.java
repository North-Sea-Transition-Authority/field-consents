package uk.co.nstauthority.fieldconsents.production;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;
import uk.co.nstauthority.fieldconsents.production.annual.AnnualProductionMonth;
import uk.co.nstauthority.fieldconsents.production.longterm.LongTermProductionYear;
import uk.co.nstauthority.fieldconsents.production.shortterm.ShortTermProductionMonth;

public record ProductionRowView(
    String rowPrompt,
    String consentDays,
    String oilMinValue,
    String oilMaxValue,
    String gasMinValue,
    String gasMaxValue
) {

  public static ProductionRowView from(String rowPrompt,
                                       Integer consentDays,
                                       BigDecimal oilMinValue,
                                       BigDecimal oilMaxValue,
                                       BigDecimal gasMinValue,
                                       BigDecimal gasMaxValue) {
    return new ProductionRowView(
        rowPrompt,
        consentDays != null ? String.valueOf(consentDays) : null,
        DecimalFormatUtils.bigDecimalToFormattedString(oilMinValue),
        DecimalFormatUtils.bigDecimalToFormattedString(oilMaxValue),
        DecimalFormatUtils.bigDecimalToFormattedString(gasMinValue),
        DecimalFormatUtils.bigDecimalToFormattedString(gasMaxValue)
    );
  }

  public static ProductionRowView fromShortTerm(ShortTermProductionMonth productionMonth) {
    return new ProductionRowView(
        DateUtils.formatShort(productionMonth.getMonth(), productionMonth.getYear()),
        String.valueOf(DateUtils.daysBetweenInclusive(productionMonth.getStartDate(), productionMonth.getEndDate())),
        productionMonth.getOilMinValueString(),
        productionMonth.getOilMaxValueString(),
        productionMonth.getGasMinValueString(),
        productionMonth.getGasMaxValueString()
    );
  }

  public static ProductionRowView fromAnnual(AnnualProductionMonth productionMonth) {
    return new ProductionRowView(
        DateUtils.formatFull(productionMonth.getMonth()),
        String.valueOf(YearMonth.of(productionMonth.getYear(), productionMonth.getMonth()).lengthOfMonth()),
        productionMonth.getOilMinValueString(),
        productionMonth.getOilMaxValueString(),
        productionMonth.getGasMinValueString(),
        productionMonth.getGasMaxValueString()
    );
  }

  public static ProductionRowView fromLongTerm(LongTermProductionYear productionYear) {
    return new ProductionRowView(
        String.valueOf(productionYear.getYear()),
        String.valueOf(Year.of(productionYear.getYear()).length()),
        productionYear.getOilMinValueString(),
        productionYear.getOilMaxValueString(),
        productionYear.getGasMinValueString(),
        productionYear.getGasMaxValueString()
    );
  }
}
