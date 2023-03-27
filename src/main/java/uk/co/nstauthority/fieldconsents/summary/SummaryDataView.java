package uk.co.nstauthority.fieldconsents.summary;

import java.util.ArrayList;
import java.util.List;

public record SummaryDataView(
    List<SummaryKeyValue> keyValues
) {
  public static SummaryDataView newWithKeyValue(String key, Object value) {
    var summaryDataView = new SummaryDataView(new ArrayList<>());
    return summaryDataView.addKeyValue(key, value);
  }

  public SummaryDataView addKeyValue(String key, Object value) {
    keyValues.add(new SummaryKeyValue(key, SummaryUtil.format(value)));
    return this;
  }
}
