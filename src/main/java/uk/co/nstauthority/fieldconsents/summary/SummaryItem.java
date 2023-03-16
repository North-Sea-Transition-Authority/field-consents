package uk.co.nstauthority.fieldconsents.summary;

import java.util.List;

public record SummaryItem(
    String displayName,
    List<SummaryGroup> summaryGroups
) {

  public static SummaryItem withGroup(String displayName,
                                      SummaryGroup summaryGroup) {
    return new SummaryItem(
        displayName,
        summaryGroup != null
            ? List.of(summaryGroup)
            : SummaryGroup.emptySummaryGroupList()
    );
  }

  public static SummaryItem withGroups(String displayName,
                                       List<SummaryGroup> summaryGroups) {
    return new SummaryItem(
        displayName,
        summaryGroups != null && !summaryGroups.isEmpty()
            ? summaryGroups
            : SummaryGroup.emptySummaryGroupList()
    );
  }
}
