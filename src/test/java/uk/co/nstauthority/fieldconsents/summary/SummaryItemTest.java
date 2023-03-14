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
  void withGroup_notEmpty() {
    assertThat(SummaryItem.withGroup(ITEM_DISPLAY_NAME, SummaryGroup.simpleSummaryGroup(keyValues)))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, List.of(SummaryGroup.simpleSummaryGroup(keyValues))));
  }

  @Test
  void withGroup_emptyNullInput() {
    assertThat(SummaryItem.withGroup(ITEM_DISPLAY_NAME, null))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, Collections.emptyList()));
  }

  @Test
  void withGroups_notEmpty() {
    List<SummaryGroup<?>> summaryGroups =
        List.of(
            SummaryGroup.simpleSummaryGroup(keyValues),
            SummaryGroup.simpleSummaryGroup(keyValues)
        );
    assertThat(SummaryItem.withGroups(ITEM_DISPLAY_NAME, summaryGroups))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, summaryGroups));
  }

  @Test
  void withGroups_empty() {
    assertThat(SummaryItem.withGroups(ITEM_DISPLAY_NAME, Collections.emptyList()))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, Collections.emptyList()));
  }

  @Test
  void withGroups_emptyNullInput() {
    assertThat(SummaryItem.withGroups(ITEM_DISPLAY_NAME, null))
        .isEqualTo(new SummaryItem(ITEM_DISPLAY_NAME, Collections.emptyList()));
  }
}