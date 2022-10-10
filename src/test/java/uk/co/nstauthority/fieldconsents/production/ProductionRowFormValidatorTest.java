package uk.co.nstauthority.fieldconsents.production;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.input.DecimalInput;
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
    DecimalInput newMaxValue = new DecimalInput("oilMaxValue", "Oil max value");
    newMaxValue.setInputValue(null);
    form.setOilMaxValue(newMaxValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("oilMaxValue.inputValue", Collections.singletonList("Oil max value must have a value."))
    );
  }

  @Test
  void validate_formWithNonNumericalField() {
    DecimalInput newMaxValue = new DecimalInput("oilMaxValue", "Oil max value");
    newMaxValue.setInputValue("test");
    form.setOilMaxValue(newMaxValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("oilMaxValue.inputValue", Collections.singletonList("Oil max value must be a number with decimal places."))
    );
  }

  @Test
  void validate_formWithNegativeValue() {
    DecimalInput newMinValue = new DecimalInput("oilMinValue", "Oil min value");
    newMinValue.setInputValue("-0.5");
    form.setOilMinValue(newMinValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("oilMinValue.inputValue", Collections.singletonList("Oil min value must be at least 0"))
    );
  }

  @Test
  void validate_formWithOilMinValueGreaterThanMax() {
    DecimalInput newMinValue = new DecimalInput("oilMinValue", "Oil min value");
    newMinValue.setInputValue("3.2");
    form.setOilMinValue(newMinValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("oilMinValue.inputValue", Collections.singletonList("Oil min value must be no more than " + form.getOilMaxValue().getInputValue()))
    );
  }

  @Test
  void validate_formWithGasMinValueGreaterThanMax() {
    DecimalInput newMinValue = new DecimalInput("gasMinValue", "Gas min value");
    newMinValue.setInputValue("5.2");
    form.setGasMinValue(newMinValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("gasMinValue.inputValue", Collections.singletonList("Gas min value must be no more than " + form.getGasMaxValue().getInputValue()))
    );
  }
}