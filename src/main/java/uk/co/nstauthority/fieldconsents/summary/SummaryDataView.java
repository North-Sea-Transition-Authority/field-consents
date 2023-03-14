package uk.co.nstauthority.fieldconsents.summary;

import java.util.List;

public record SummaryDataView(
    List<SummaryKeyValue> keyValues
) {
  public static SummaryDataView from(List<SummaryKeyValue> keyValues) {
    return new SummaryDataView(keyValues);
  }
}
