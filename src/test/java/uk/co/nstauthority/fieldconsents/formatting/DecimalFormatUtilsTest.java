package uk.co.nstauthority.fieldconsents.formatting;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecimalFormatUtilsTest {

  @ParameterizedTest
  @CsvSource({
      "0,£0.00",
      "0.25,£0.25",
      "1,£1.00",
      "1.5,£1.50",
      "10,£10.00",
      "14.52,£14.52",
      "100,£100.00",
      "170.77,£170.77",
      "1000,'£1,000.00'",
      "1180,'£1,180.00'",
  })
  void formatMoney(double amount, String expected) {
    assertThat(DecimalFormatUtils.formatMoney(amount)).isEqualTo(expected);
  }
}
