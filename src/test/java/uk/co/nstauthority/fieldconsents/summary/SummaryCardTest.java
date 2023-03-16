package uk.co.nstauthority.fieldconsents.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SummaryCardTest {

  private final List<SummaryKeyValue> keyValues = List.of(
      new SummaryKeyValue("key1", "value1"),
      new SummaryKeyValue("key2", "value2")
  );

  @Test
  void simpleSummaryCardWithHeading() {
    assertThat(SummaryCard.simpleSummaryCardWithHeading("group display name", keyValues))
        .isEqualTo(
            new SummaryCard(
                "group display name",
                SummaryCardType.SIMPLE_SUMMARY,
                SummaryDataView.from(keyValues)
            )
        );
  }

  @Test
  void simpleSummaryCard() {
    assertThat(SummaryCard.simpleSummaryCard(keyValues))
        .isEqualTo(
            new SummaryCard(
                null,
                SummaryCardType.SIMPLE_SUMMARY,
                SummaryDataView.from(keyValues)
            )
        );
  }

  @Test
  void emptySummaryCard() {
    assertThat(SummaryCard.emptySummaryCard())
        .isEqualTo(
          new SummaryCard(
              null,
              SummaryCardType.EMPTY_SUMMARY,
              null
          )
        );
  }

  @Test
  void emptySummaryCardList() {
    assertThat(SummaryCard.emptySummaryCardList())
        .isEqualTo(
            List.of(
                new SummaryCard(
                    null,
                    SummaryCardType.EMPTY_SUMMARY,
                    null
                )
            )
        );
  }
}