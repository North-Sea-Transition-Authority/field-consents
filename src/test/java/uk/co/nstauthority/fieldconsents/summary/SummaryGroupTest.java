package uk.co.nstauthority.fieldconsents.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SummaryGroupTest {

  private final List<SummaryKeyValue> keyValues = List.of(
      new SummaryKeyValue("key1", "value1"),
      new SummaryKeyValue("key2", "value2")
  );

  @Test
  void simpleSummaryGroupWithHeading() {
    assertThat(SummaryGroup.simpleSummaryGroupWithHeading("group display name", keyValues))
        .isEqualTo(
            new SummaryGroup(
                "group display name",
                SummaryGroupType.SIMPLE_SUMMARY,
                SummaryDataView.from(keyValues)
            )
        );
  }

  @Test
  void simpleSummaryGroup() {
    assertThat(SummaryGroup.simpleSummaryGroup(keyValues))
        .isEqualTo(
            new SummaryGroup(
                null,
                SummaryGroupType.SIMPLE_SUMMARY,
                SummaryDataView.from(keyValues)
            )
        );
  }

  @Test
  void emptySummaryGroup() {
    assertThat(SummaryGroup.emptySummaryGroup())
        .isEqualTo(
          new SummaryGroup(
              null,
              SummaryGroupType.EMPTY_SUMMARY,
              null
          )
        );
  }

  @Test
  void emptySummaryGroupList() {
    assertThat(SummaryGroup.emptySummaryGroupList())
        .isEqualTo(
            List.of(
                new SummaryGroup(
                    null,
                    SummaryGroupType.EMPTY_SUMMARY,
                    null
                )
            )
        );
  }
}