package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class ConsentProductionFiguresInputValidatorTest {

  private ConsentProductionFiguresInput input;

  private ConsentProductionFiguresInputValidator validator;

  private Errors errors;
  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    input = new ConsentProductionFiguresInput();
    input.getMinOilInput().setInputValue("0.5");
    input.getMaxOilInput().setInputValue("2.3");
    input.getMinGasInput().setInputValue("1.72");
    input.getMaxGasInput().setInputValue("4.25");

    validator = new ConsentProductionFiguresInputValidator();
    errors = new BeanPropertyBindingResult(input, "form");
  }

  @Test
  void validate_completeAndValidForm() {
    ValidationUtils.invokeValidator(validator, input, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_formWithEmptyField() {
    input.getMaxOilInput().setInputValue(null);

    ValidationUtils.invokeValidator(validator, input, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("maxOilInput.inputValue", Collections.singletonList("Enter maximum oil"))
    );
  }

  @Test
  void validate_formWithNonNumericalField() {
    input.getMaxOilInput().setInputValue("test");

    ValidationUtils.invokeValidator(validator, input, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("maxOilInput.inputValue", Collections.singletonList("Maximum oil must be a number"))
    );
  }

  @Test
  void validate_formWithNegativeValue() {
    input.getMinOilInput().setInputValue("-0.5");

    ValidationUtils.invokeValidator(validator, input, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("minOilInput.inputValue", Collections.singletonList("Minimum oil must be 0 or more"))
    );
  }

  @Test
  void validate_formWithminOilInputGreaterThanMax() {
    input.getMinOilInput().setInputValue("3.2");

    ValidationUtils.invokeValidator(validator, input, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("maxOilInput.inputValue",
            Collections.singletonList("Maximum oil must be %s or more".formatted(input.getMinOilInput().getInputValue())))
    );
  }

  @Test
  void validate_formWithminGasInputGreaterThanMax() {
    input.getMinGasInput().setInputValue("5.2");

    ValidationUtils.invokeValidator(validator, input, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("maxGasInput.inputValue",
            Collections.singletonList("Maximum gas must be %s or more".formatted(input.getMinGasInput().getInputValue())))
    );
  }

  @Test
  void validate_formWithminGasInputGreaterThanMax_andOilValuesNull() {
    input.getMinOilInput().setInputValue(null);
    input.getMaxOilInput().setInputValue(null);
    input.getMinGasInput().setInputValue("5.2");

    ValidationUtils.invokeValidator(validator, input, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("minOilInput.inputValue", Collections.singletonList("Enter minimum oil")),
        entry("maxOilInput.inputValue", Collections.singletonList("Enter maximum oil")),
        entry("maxGasInput.inputValue",
            Collections.singletonList("Maximum gas must be %s or more".formatted(input.getMinGasInput().getInputValue())))
    );
  }

  @Test
  void validate_formWithminOilInputGreaterThanMax_andGasValuesNull() {
    input.getMinOilInput().setInputValue("3.2");
    input.getMinGasInput().setInputValue(null);
    input.getMaxGasInput().setInputValue(null);

    ValidationUtils.invokeValidator(validator, input, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("maxOilInput.inputValue",
            Collections.singletonList("Maximum oil must be %s or more".formatted(input.getMinOilInput().getInputValue()))),
        entry("minGasInput.inputValue", Collections.singletonList("Enter minimum gas")),
        entry("maxGasInput.inputValue", Collections.singletonList("Enter maximum gas"))
    );
  }

  @Test
  void validate_formWithTooManyDecimalPlace() {
    String productionValue = "1.1234567";
    input.getMinOilInput().setInputValue(productionValue);
    input.getMaxOilInput().setInputValue(productionValue);
    input.getMinGasInput().setInputValue(productionValue);
    input.getMaxGasInput().setInputValue(productionValue);

    ValidationUtils.invokeValidator(validator, input, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("minOilInput.inputValue",
            Collections.singletonList("Minimum oil must include no more than 6 decimal places")),
        entry("maxOilInput.inputValue",
            Collections.singletonList("Maximum oil must include no more than 6 decimal places")),
        entry("minGasInput.inputValue",
            Collections.singletonList("Minimum gas must include no more than 6 decimal places")),
        entry("maxGasInput.inputValue",
            Collections.singletonList("Maximum gas must include no more than 6 decimal places"))
    );
  }

  @Test
  void validate_formWithMaxDecimalPlaces() {
    String productionValue = "1.123456";
    input.getMinOilInput().setInputValue(productionValue);
    input.getMaxOilInput().setInputValue(productionValue);
    input.getMinGasInput().setInputValue(productionValue);
    input.getMaxGasInput().setInputValue(productionValue);

    ValidationUtils.invokeValidator(validator, input, errors);

    assertThat(errors.hasErrors()).isFalse();
  }
}
