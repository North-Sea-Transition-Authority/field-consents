package uk.co.nstauthority.fieldconsents.summary;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SummaryDataViewTest {

  List<SummaryKeyValue> keyValues1 = List.of(
      new SummaryKeyValue("key1", "value1"),
      new SummaryKeyValue("key2", "value2"),
      new SummaryKeyValue("key3", "value3")
  );

  @Test
  void from() {
    assertThat(SummaryDataView.from(keyValues1))
        .isEqualTo(new SummaryDataView(keyValues1));
  }
}