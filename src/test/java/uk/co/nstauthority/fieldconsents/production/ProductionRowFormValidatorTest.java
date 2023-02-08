package uk.co.nstauthority.fieldconsents.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class ProductionRowFormValidatorTest {

  private ProductionRowForm form;

  private ProductionRowFormValidator validator;

  private Errors errors;
  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    form = ProductionTestUtils.getCompleteAnnualProductionMonthForm(Month.OCTOBER);
    validator = new ProductionRowFormValidator();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void validate_completeAndValidForm() {
    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_formWithEmptyField() {
    form.getOilMaxValue().setInputValue(null);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("oilMaxValue.inputValue", Collections.singletonList("Enter Maximum oil"))
    );
  }

  @Test
  void validate_formWithNonNumericalField() {
    form.getOilMaxValue().setInputValue("test");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("oilMaxValue.inputValue", Collections.singletonList("Maximum oil must be a number"))
    );
  }

  @Test
  void validate_formWithNegativeValue() {
    form.getOilMinValue().setInputValue("-0.5");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("oilMinValue.inputValue", Collections.singletonList("Minimum oil must be 0 or more"))
    );
  }

  @Test
  void validate_formWithOilMinValueGreaterThanMax() {
    form.getOilMinValue().setInputValue("3.2");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("oilMaxValue.inputValue",
            Collections.singletonList("Maximum oil must be %s or more".formatted(form.getOilMinValue().getInputValue())))
    );
  }

  @Test
  void validate_formWithGasMinValueGreaterThanMax() {
    form.getGasMinValue().setInputValue("5.2");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("gasMaxValue.inputValue",
            Collections.singletonList("Maximum gas must be %s or more".formatted(form.getGasMinValue().getInputValue())))
    );
  }

  @Test
  void validate_formWithGasMinValueGreaterThanMax_andOilValuesNull() {
    form.getOilMinValue().setInputValue(null);
    form.getOilMaxValue().setInputValue(null);
    form.getGasMinValue().setInputValue("5.2");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("oilMinValue.inputValue", Collections.singletonList("Enter Minimum oil")),
        entry("oilMaxValue.inputValue", Collections.singletonList("Enter Maximum oil")),
        entry("gasMaxValue.inputValue",
            Collections.singletonList("Maximum gas must be %s or more".formatted(form.getGasMinValue().getInputValue())))
    );
  }

  @Test
  void validate_formWithOilMinValueGreaterThanMax_andGasValuesNull() {
    form.getOilMinValue().setInputValue("3.2");
    form.getGasMinValue().setInputValue(null);
    form.getGasMaxValue().setInputValue(null);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("oilMaxValue.inputValue",
            Collections.singletonList("Maximum oil must be %s or more".formatted(form.getOilMinValue().getInputValue()))),
        entry("gasMinValue.inputValue", Collections.singletonList("Enter Minimum gas")),
        entry("gasMaxValue.inputValue", Collections.singletonList("Enter Maximum gas"))
    );
  }

  @Test
  void validate_formWithTooManyDecimalPlace() {
    String productionValue = "1.1234567";
    form.getOilMinValue().setInputValue(productionValue);
    form.getOilMaxValue().setInputValue(productionValue);
    form.getGasMinValue().setInputValue(productionValue);
    form.getGasMaxValue().setInputValue(productionValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("oilMinValue.inputValue",
            Collections.singletonList("Minimum oil must include no more than 6 decimal places")),
        entry("oilMaxValue.inputValue",
            Collections.singletonList("Maximum oil must include no more than 6 decimal places")),
        entry("gasMinValue.inputValue",
            Collections.singletonList("Minimum gas must include no more than 6 decimal places")),
        entry("gasMaxValue.inputValue",
            Collections.singletonList("Maximum gas must include no more than 6 decimal places"))
    );
  }

  @Test
  void validate_formWithMaxDecimalPlaces() {
    String productionValue = "1.123456";
    form.getOilMinValue().setInputValue(productionValue);
    form.getOilMaxValue().setInputValue(productionValue);
    form.getGasMinValue().setInputValue(productionValue);
    form.getGasMaxValue().setInputValue(productionValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }
}