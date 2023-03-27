package uk.co.nstauthority.fieldconsents.summary;

import java.math.BigDecimal;
import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

class SummaryUtil {
  private SummaryUtil() {
    throw new IllegalStateException("Utility class");
  }

  static String format(Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof String stringValue) {
      return stringValue;
    } else if (value instanceof Integer integerValue) {
      return String.valueOf(integerValue);
    } else if (value instanceof Boolean booleanValue) {
      return BooleanUtil.yesNoFromBoolean(booleanValue);
    } else if (value instanceof LocalDate localDateValue) {
      return DateUtils.format(localDateValue, DateUtils.SHORT_DATE);
    } else if (value instanceof BigDecimal bigDecimalValue) {
      return DecimalFormatUtils.bigDecimalToFormattedString(bigDecimalValue);
    } else {
      throw new RuntimeException("Unexpected value class type: " + value.getClass().getName());
    }
  }
}