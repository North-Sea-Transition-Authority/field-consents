package uk.co.nstauthority.fieldconsents.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

class SummaryUtilTest {

  private static final String UNEXPECTED_CLASS_EXCEPTION_MESSAGE = "Unexpected value class type: %s";

  @Test
  void format_null() {
    assertThat(SummaryUtil.format(null))
        .isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"Test 1", "test 2", "TEST 3"})
  void format_String(String stringValue) {
    assertThat(SummaryUtil.format(stringValue))
        .isEqualTo(stringValue);
  }

  @ParameterizedTest
  @ValueSource(ints = {-999999, -1, 0, 1, 999999})
  void format_Integer(Integer integerValue) {
    assertThat(SummaryUtil.format(integerValue))
        .isEqualTo(String.valueOf(integerValue));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void format_Boolean(Boolean booleanValue) {
    assertThat(SummaryUtil.format(booleanValue))
        .isEqualTo(BooleanUtil.yesNoFromBoolean(booleanValue));
  }

  @Test
  void format_LocalDate() {
    LocalDate localDateValue = LocalDate.now();
    assertThat(SummaryUtil.format(localDateValue))
        .isEqualTo(DateUtils.format(localDateValue, DateUtils.SHORT_DATE));
  }

  @Test
  void format_BigDecimal() {
    BigDecimal bigDecimalValue = BigDecimal.valueOf(1.123456789);
    assertThat(SummaryUtil.format(bigDecimalValue))
        .isEqualTo(DecimalFormatUtils.bigDecimalToFormattedString(bigDecimalValue));
  }

  @ParameterizedTest
  @MethodSource("getUnsupportedValuesOfClass")
  void format_expectException(Object value) {
    assertThatThrownBy(() -> SummaryUtil.format(value))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(UNEXPECTED_CLASS_EXCEPTION_MESSAGE.formatted(value.getClass().getName()));
  }

  private static Stream<Arguments> getUnsupportedValuesOfClass() {
    return Stream.of(
        Arguments.of(1L),
        Arguments.of(1.0)
    );
  }
}