package uk.co.nstauthority.fieldconsents.charts;

import static uk.co.nstauthority.fieldconsents.charts.HighchartsColour.DARK_BLUE;
import static uk.co.nstauthority.fieldconsents.charts.HighchartsColour.LIGHT_BLUE;

import java.math.BigDecimal;
import java.util.function.Function;
import uk.co.nstauthority.fieldconsents.production.ProductionRow;

public enum ProductionType {

  OIL(
      "Production min/max for oil",
      DARK_BLUE,
      ProductionRow::getOilMinValue,
      ProductionRow::getOilMaxValue
  ),
  GAS(
      "Production min/max for gas",
      LIGHT_BLUE,
      ProductionRow::getGasMinValue,
      ProductionRow::getGasMaxValue
  ),
  ;

  private final String seriesName;
  private final String highchartsColour;
  private final Function<ProductionRow, BigDecimal> minimumFunction;
  private final Function<ProductionRow, BigDecimal> maximumFunction;

  ProductionType(
      String seriesName,
      String highchartsColour,
      Function<ProductionRow, BigDecimal> minimumFunction,
      Function<ProductionRow, BigDecimal> maximumFunction
  ) {
    this.seriesName = seriesName;
    this.highchartsColour = highchartsColour;
    this.minimumFunction = minimumFunction;
    this.maximumFunction = maximumFunction;
  }

  public String getSeriesName() {
    return seriesName;
  }

  public String getHighchartsColour() {
    return highchartsColour;
  }

  public Function<ProductionRow, BigDecimal> getMinimumFunction() {
    return minimumFunction;
  }

  public Function<ProductionRow, BigDecimal> getMaximumFunction() {
    return maximumFunction;
  }
}
