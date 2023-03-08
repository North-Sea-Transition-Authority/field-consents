package uk.co.nstauthority.fieldconsents.summary;

public record SummaryItem<T>(
    String displayName,
    SummaryItemType summaryItemType,
    Class<T> clazz,
    T summaryData
) {

  public static SummaryItem<SummaryDataView> simpleSummaryItem(String displayName,
                                                               SummaryDataView summaryData) {
    return new SummaryItem<>(
        displayName,
        SummaryItemType.SIMPLE_SUMMARY,
        SummaryDataView.class,
        summaryData
    );
  }
}
