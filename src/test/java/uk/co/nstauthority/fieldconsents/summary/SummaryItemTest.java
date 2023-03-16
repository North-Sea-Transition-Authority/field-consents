package uk.co.nstauthority.fieldconsents.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class SummaryItemTest {

  private static final String ITEM_DISPLAY_NAME = "Item display name";

  private final List<SummaryKeyValue> keyValues = List.of(
      new SummaryKeyValue("key1", "value1"),
      new SummaryKeyValue("key2", "value2")
  );

  @Test
  void withCard_notEmpty() {
    assertThat(SummaryItem.withCard(ITEM_DISPLAY_NAME, SummaryCard.simpleSummaryCard(keyValues)))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, List.of(SummaryCard.simpleSummaryCard(keyValues))));
  }

  @Test
  void withCard_emptyNullInput() {
    assertThat(SummaryItem.withCard(ITEM_DISPLAY_NAME, null))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, SummaryCard.emptySummaryCardList()));
  }

  @Test
  void withCards_notEmpty() {
    List<SummaryCard> summaryCards =
        List.of(
            SummaryCard.simpleSummaryCard(keyValues),
            SummaryCard.simpleSummaryCard(keyValues)
        );
    assertThat(SummaryItem.withCards(ITEM_DISPLAY_NAME, summaryCards))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, summaryCards));
  }

  @Test
  void withCards_empty() {
    assertThat(SummaryItem.withCards(ITEM_DISPLAY_NAME, Collections.emptyList()))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, SummaryCard.emptySummaryCardList()));
  }

  @Test
  void withCards_emptyNullInput() {
    assertThat(SummaryItem.withCards(ITEM_DISPLAY_NAME, null))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, SummaryCard.emptySummaryCardList()));
  }
}