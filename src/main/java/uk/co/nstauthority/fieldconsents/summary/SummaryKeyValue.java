package uk.co.nstauthority.fieldconsents.summary;

import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

public record SummaryKeyValue(
    String key,
    String value
) {
  public static SummaryKeyValue from(String key, String value) {
    return new SummaryKeyValue(key, value);
  }

  public static SummaryKeyValue fromKeyNoValue(String key) {
    return new SummaryKeyValue(key, null);
  }

  public static SummaryKeyValue fromLocalDate(String key, LocalDate localDateValue) {
    return new SummaryKeyValue(key, DateUtils.format(localDateValue, DateUtils.SHORT_DATE));
  }

  public static SummaryKeyValue fromBoolean(String key, Boolean booleanValue) {
    return new SummaryKeyValue(key, BooleanUtil.yesNoFromBoolean(booleanValue, ""));
  }

  public static SummaryKeyValue fromInteger(String key, Integer integerValue) {
    return new SummaryKeyValue(key, String.valueOf(integerValue));
  }
}
