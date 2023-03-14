package uk.co.nstauthority.fieldconsents.summary;

import java.util.List;

public record SummaryGroup<T>(
    String displayName,
    SummaryGroupType summaryGroupType,
    Class<T> clazz,
    T summaryData
) {

  public static SummaryGroup<SummaryDataView> simpleSummaryGroupWithHeading(String displayName,
                                                                            List<SummaryKeyValue> keyValues) {
    return new SummaryGroup<>(
        displayName,
        SummaryGroupType.SIMPLE_SUMMARY,
        SummaryDataView.class,
        SummaryDataView.from(keyValues)
    );
  }

  public static SummaryGroup<SummaryDataView> simpleSummaryGroup(List<SummaryKeyValue> keyValues) {
    return simpleSummaryGroupWithHeading(null, keyValues);
  }
}