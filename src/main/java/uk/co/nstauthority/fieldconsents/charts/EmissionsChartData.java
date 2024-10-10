package uk.co.nstauthority.fieldconsents.charts;

import java.util.List;

public record EmissionsChartData(
    String title,
    List<String> xAxisCategories,
    String xAxisTitleText,
    String yAxisTitleText,
    List<Series> series
) {

  public record Series(
      String name,
      String className,
      List<DataPoint> data
  ) {
  }

  public record DataPoint(
      String className,
      Number y
  ) {
  }

}
