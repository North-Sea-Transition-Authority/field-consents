package uk.co.nstauthority.fieldconsents.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class SummaryDataViewTest {

  private final String KEY1 = "key1";
  private final String VALUE1 = "value1";
  private final String KEY2 = "key2";
  private final String VALUE2 = "value2";
  private final String KEY3 = "key3";
  private final String VALUE3 = "value3";

  private final List<SummaryKeyValue> summaryKeyValues = List.of(
      new SummaryKeyValue(KEY1, VALUE1),
      new SummaryKeyValue(KEY2, VALUE2),
      new SummaryKeyValue(KEY3, VALUE3)
  );

  @Test
  void newWithKeyValue() {
    assertThat(SummaryDataView.newWithKeyValue(KEY1, VALUE1))
        .isEqualTo(new SummaryDataView(List.of(new SummaryKeyValue(KEY1, VALUE1))));
  }

  @Test
  void newWithKeyValue_plus_addKeyValue() {
    assertThat(
        SummaryDataView
            .newWithKeyValue(KEY1, VALUE1)
            .addKeyValue(KEY2, VALUE2)
            .addKeyValue(KEY3, VALUE3)
    ).isEqualTo(new SummaryDataView(summaryKeyValues));
  }

  @Test
  void newWithKeyValue_formatError() {
    assertThatThrownBy(() -> SummaryDataView.newWithKeyValue(KEY1, 1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unexpected value class type: %s".formatted(Long.class.getName()));
  }

  @Test
  void addKeyValue_formatError() {
    var summaryData = SummaryDataView.newWithKeyValue(KEY1, VALUE1);
    assertThatThrownBy(() -> summaryData.addKeyValue(KEY2, 1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Unexpected value class type: %s".formatted(Long.class.getName()));
  }
}