package uk.co.nstauthority.fieldconsents.summary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import uk.co.nstauthority.fieldconsents.charts.EmissionsChartData;

public record SummaryCard(
    String displayName,
    SummaryCardType summaryCardType,
    Object summaryData
) {

  public static SummaryCard simpleSummaryCardWithHeading(String displayName,
                                                         SummaryDataView summaryData) {
    return new SummaryCard(
        displayName,
        SummaryCardType.SIMPLE_SUMMARY,
        summaryData
    );
  }

  public static SummaryCard simpleSummaryCard(SummaryDataView summaryData) {
    return simpleSummaryCardWithHeading(null, summaryData);
  }

  public static SummaryCard emptySummaryCard() {
    return new SummaryCard(
        null,
        SummaryCardType.EMPTY_SUMMARY,
        null
    );
  }

  public static List<SummaryCard> emptySummaryCardList() {
    return List.of(emptySummaryCard());
  }

  public static SummaryCard tableSummaryCardWithHeading(String displayName,
                                                        SummaryTableView summaryData) {
    return new SummaryCard(
        displayName,
        SummaryCardType.TABLE_SUMMARY,
        summaryData
    );
  }

  public static SummaryCard tableSummaryCard(SummaryTableView summaryData) {
    return tableSummaryCardWithHeading(null, summaryData);
  }

  public static SummaryCard filesSummaryCardWithHeading(String heading, List<SummaryFileView> fileViews) {
    return new SummaryCard(
        heading,
        SummaryCardType.FILES_SUMMARY,
        fileViews
    );
  }

  public static SummaryCard stackedBarChartSummaryCard(EmissionsChartData emissionsChartData) {
    var objectMapper = new ObjectMapper();
    try {
      var chartDataJson = objectMapper.writeValueAsString(emissionsChartData);
      return new SummaryCard(null, SummaryCardType.STACKED_BAR_CHART_SUMMARY, chartDataJson);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialise chart data into JSON");
    }
  }

}
