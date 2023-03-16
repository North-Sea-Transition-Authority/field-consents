package uk.co.nstauthority.fieldconsents.summary;

import java.util.List;

public record SummaryGroup(
    String displayName,
    SummaryGroupType summaryGroupType,
    Object summaryData
) {

  public static SummaryGroup simpleSummaryGroupWithHeading(String displayName,
                                                           List<SummaryKeyValue> keyValues) {
    return new SummaryGroup(
        displayName,
        SummaryGroupType.SIMPLE_SUMMARY,
        SummaryDataView.from(keyValues)
    );
  }

  public static SummaryGroup simpleSummaryGroup(List<SummaryKeyValue> keyValues) {
    return simpleSummaryGroupWithHeading(null, keyValues);
  }

  public static SummaryGroup emptySummaryGroup() {
    return new SummaryGroup(
        null,
        SummaryGroupType.EMPTY_SUMMARY,
        null
    );
  }

  public static List<SummaryGroup> emptySummaryGroupList() {
    return List.of(emptySummaryGroup());
  }
}