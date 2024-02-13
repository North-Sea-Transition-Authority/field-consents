package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsentProductionFiguresInputTest {

  private ConsentProductionFiguresInput consentProductionFiguresInput;

  @BeforeEach
  void beforeEach() {
    consentProductionFiguresInput = new ConsentProductionFiguresInput();
  }

  @Test
  void withDefaultValuesFromDto() {
    var consentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    assertThat(ConsentProductionFiguresInput.withDefaultValuesFromDto(consentProductionFiguresDto).getAsDtoOrThrow())
        .isEqualTo(consentProductionFiguresDto);
  }

  @Test
  void getAsDtoOrThrow() {
    var minOil = BigDecimal.valueOf(235.79);
    var maxOil = BigDecimal.valueOf(673.12);
    var minGas = BigDecimal.valueOf(112.89);
    var maxGas = BigDecimal.valueOf(456.99);

    consentProductionFiguresInput.getMinOilInput().setInputValue(bigDecimalToFormattedString(minOil));
    consentProductionFiguresInput.getMaxOilInput().setInputValue(bigDecimalToFormattedString(maxOil));
    consentProductionFiguresInput.getMinGasInput().setInputValue(bigDecimalToFormattedString(minGas));
    consentProductionFiguresInput.getMaxGasInput().setInputValue(bigDecimalToFormattedString(maxGas));

    assertThat(consentProductionFiguresInput.getAsDtoOrThrow())
        .isEqualTo(new ConsentProductionFiguresDto(minOil, maxOil, minGas, maxGas));
  }

  @Test
  void setInputValuesFromDto() {
    var consentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    var minOil = consentProductionFiguresDto.minOil();
    var maxOil = consentProductionFiguresDto.maxOil();
    var minGas = consentProductionFiguresDto.minGas();
    var maxGas = consentProductionFiguresDto.maxGas();

    consentProductionFiguresInput.getMinOilInput().setInputValue(bigDecimalToFormattedString(minOil));
    consentProductionFiguresInput.getMaxOilInput().setInputValue(bigDecimalToFormattedString(maxOil));
    consentProductionFiguresInput.getMinGasInput().setInputValue(bigDecimalToFormattedString(minGas));
    consentProductionFiguresInput.getMaxGasInput().setInputValue(bigDecimalToFormattedString(maxGas));

    consentProductionFiguresInput.setInputValuesFromDto(consentProductionFiguresDto);

    assertThat(consentProductionFiguresInput.getMinOilInput().getAsBigDecimal())
        .isPresent()
        .contains(minOil);

    assertThat(consentProductionFiguresInput.getMaxOilInput().getAsBigDecimal())
        .isPresent()
        .contains(maxOil);

    assertThat(consentProductionFiguresInput.getMinGasInput().getAsBigDecimal())
        .isPresent()
        .contains(minGas);

    assertThat(consentProductionFiguresInput.getMaxGasInput().getAsBigDecimal())
        .isPresent()
        .contains(maxGas);
  }
}
