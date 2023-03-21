package uk.co.nstauthority.fieldconsents.summary;

import java.util.List;

public record SummaryCard(
    String displayName,
    SummaryCardType summaryCardType,
    Object summaryData
) {

  public static SummaryCard simpleSummaryCardWithHeading(String displayName,
                                                          List<SummaryKeyValue> keyValues) {
    return new SummaryCard(
        displayName,
        SummaryCardType.SIMPLE_SUMMARY,
        SummaryDataView.from(keyValues)
    );
  }

  public static SummaryCard simpleSummaryCard(List<SummaryKeyValue> keyValues) {
    return simpleSummaryCardWithHeading(null, keyValues);
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
}