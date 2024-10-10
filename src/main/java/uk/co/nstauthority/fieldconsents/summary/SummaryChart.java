package uk.co.nstauthority.fieldconsents.summary;

public record SummaryChart(
    SummaryChartType chartType,
    String chartDataJson
) {
}
