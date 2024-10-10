package uk.co.nstauthority.fieldconsents.charts;

import java.util.List;

public record ProductionChartData(
    String title,
    List<String> xAxisCategories,
    String xAxisTitleText,
    String yAxisTitleText,
    List<Series> series
) {

  public record Series(
      String name,
      //The className is declared on both the series and the data points.
      //On the series it controls the colours displayed on the legend and the tooltip
      //On the data points it controls the colour of the bar
      //Highcharts automatically adds an auto-enumerated class, with associated colour styling to both
      //The Highcharts auto-enumeration overrides our custom series className on each data point
      //So both need to be set for consistency
      String className,
      String type,
      List<DataPoint> data
  ) {
  }

  public record DataPoint(
      Number x,
      Number low,
      Number high,
      String className
  ) {
  }
}
