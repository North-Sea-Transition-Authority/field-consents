package uk.co.nstauthority.fieldconsents.flarevent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataFormValidator.EVALUATED_PER_CATEGORY_MISSING;
import static uk.co.nstauthority.fieldconsents.flarevent.FlareVentReportGasDataFormValidator.INVALID_GAS_CONTENT_PERCENTAGE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class FlareVentReportGasDataFormValidatorTest {

  private FlareVentReportGasDataFormValidator validator;

  private FlareVentReportGasDataForm form;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    validator = new FlareVentReportGasDataFormValidator();
    form = FlareVentReportGasTestUtil.getValidFlareVentReportGasDataForm();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void validate_completeAndValidForm() {
    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("getCategoryDensityArguments")
  void validate_formWithInvalidStandardDensityFields(String categoryDensity,
                                                     String categoryDensityError) {
    form.getCategoryADensity().setInputValue(categoryDensity);
    form.getCategoryBDensity().setInputValue(categoryDensity);
    form.getCategoryCDensity().setInputValue(categoryDensity);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("categoryADensity.inputValue", Collections.singletonList(categoryDensityError.formatted("A"))),
        entry("categoryBDensity.inputValue", Collections.singletonList(categoryDensityError.formatted("B"))),
        entry("categoryCDensity.inputValue", Collections.singletonList(categoryDensityError.formatted("C")))
    );
  }

  private static Stream<Arguments> getCategoryDensityArguments() {
    return Stream.of(
        Arguments.of(null, "Enter Category %s standard density"),
        Arguments.of("test", "Category %s standard density must be a number"),
        Arguments.of("-0.5", "Category %s standard density must be 0 or more"),
        Arguments.of("0.0000001", "Category %s standard density must include no more than 6 decimal places")
    );
  }

  @ParameterizedTest
  @MethodSource("getGasContentArguments")
  void validate_formWithInvalidGasContentFields(String gasContentValue,
                                                String inertGasContentError,
                                                String hydrocarbonContentError) {
    form.getCategoryAInertGasPercentage().setInputValue(gasContentValue);
    form.getCategoryAHydrocarbonPercentage().setInputValue(gasContentValue);
    form.getCategoryBInertGasPercentage().setInputValue(gasContentValue);
    form.getCategoryBHydrocarbonPercentage().setInputValue(gasContentValue);
    form.getCategoryCInertGasPercentage().setInputValue(gasContentValue);
    form.getCategoryCHydrocarbonPercentage().setInputValue(gasContentValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("categoryAInertGasPercentage.inputValue", Collections.singletonList(inertGasContentError.formatted("A"))),
        entry("categoryAHydrocarbonPercentage.inputValue", Collections.singletonList(hydrocarbonContentError.formatted("A"))),
        entry("categoryBInertGasPercentage.inputValue", Collections.singletonList(inertGasContentError.formatted("B"))),
        entry("categoryBHydrocarbonPercentage.inputValue", Collections.singletonList(hydrocarbonContentError.formatted("B"))),
        entry("categoryCInertGasPercentage.inputValue", Collections.singletonList(inertGasContentError.formatted("C"))),
        entry("categoryCHydrocarbonPercentage.inputValue", Collections.singletonList(hydrocarbonContentError.formatted("C")))
    );
  }

  private static Stream<Arguments> getGasContentArguments() {
    return Stream.of(
        Arguments.of(null,
            "Enter Category %s inert gas content",
            "Enter Category %s hydrocarbon content"),
        Arguments.of("test",
            "Category %s inert gas content must be a number",
            "Category %s hydrocarbon content must be a number"),
        Arguments.of("-0.5",
            "Category %s inert gas content must be 0 or more",
            "Category %s hydrocarbon content must be 0 or more"),
        Arguments.of("100.000001",
            "Category %s inert gas content must be 100 or less",
            "Category %s hydrocarbon content must be 100 or less"),
        Arguments.of("0.0000001",
            "Category %s inert gas content must include no more than 6 decimal places",
            "Category %s hydrocarbon content must include no more than 6 decimal places")
    );
  }

  @ParameterizedTest
  @MethodSource("getHydrocarbonArguments")
  void validate_formWithInvalidHydrocarbonContentFields(String inertGasContentValue,
                                                        String hydrocarbonContentValue,
                                                        String hydrocarbonContentError) {
    form.getCategoryAInertGasPercentage().setInputValue(inertGasContentValue);
    form.getCategoryAHydrocarbonPercentage().setInputValue(hydrocarbonContentValue);
    form.getCategoryBInertGasPercentage().setInputValue(inertGasContentValue);
    form.getCategoryBHydrocarbonPercentage().setInputValue(hydrocarbonContentValue);
    form.getCategoryCInertGasPercentage().setInputValue(inertGasContentValue);
    form.getCategoryCHydrocarbonPercentage().setInputValue(hydrocarbonContentValue);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("categoryAHydrocarbonPercentage.inputValue", Collections.singletonList(hydrocarbonContentError.formatted("A"))),
        entry("categoryBHydrocarbonPercentage.inputValue", Collections.singletonList(hydrocarbonContentError.formatted("B"))),
        entry("categoryCHydrocarbonPercentage.inputValue", Collections.singletonList(hydrocarbonContentError.formatted("C")))
    );
  }

  private static Stream<Arguments> getHydrocarbonArguments() {
    return Stream.of(
        Arguments.of("50",
            null,
            "Enter Category %s hydrocarbon content"),
        Arguments.of("50",
            "test",
            "Category %s hydrocarbon content must be a number"),
        Arguments.of("50",
            "-0.5",
            "Category %s hydrocarbon content must be 0 or more"),
        Arguments.of("50",
            "100.000001",
            "Category %s hydrocarbon content must be 100 or less"),
        Arguments.of("50",
            "0.0000001",
            "Category %s hydrocarbon content must include no more than 6 decimal places")
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"50.000001", "49.999999"})
  void validate_formWithCategoryAGasContentValuesInvalid(String hydrocarbonPercentage) {
    form.getCategoryAInertGasPercentage().setInputValue("50");
    form.getCategoryAHydrocarbonPercentage().setInputValue(hydrocarbonPercentage);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("categoryAInertGasPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("categoryAHydrocarbonPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE))
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"50.000001", "49.999999"})
  void validate_formWithCategoryBGasContentValuesInvalid(String hydrocarbonPercentage) {
    form.getCategoryBInertGasPercentage().setInputValue("50");
    form.getCategoryBHydrocarbonPercentage().setInputValue(hydrocarbonPercentage);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("categoryBInertGasPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("categoryBHydrocarbonPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE))
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"50.000001", "49.999999"})
  void validate_formWithCategoryCGasContentValuesInvalid(String hydrocarbonPercentage) {
    form.getCategoryCInertGasPercentage().setInputValue("50");
    form.getCategoryCHydrocarbonPercentage().setInputValue(hydrocarbonPercentage);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("categoryCInertGasPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("categoryCHydrocarbonPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE))
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"50.000001", "49.999999"})
  void validate_formWithCategoryGasContentValuesInvalid_plusOtherFormError(String hydrocarbonPercentage) {
    form.getCategoryAInertGasPercentage().setInputValue("50");
    form.getCategoryAHydrocarbonPercentage().setInputValue(hydrocarbonPercentage);
    form.getCategoryBInertGasPercentage().setInputValue("50");
    form.getCategoryBHydrocarbonPercentage().setInputValue(hydrocarbonPercentage);
    form.getCategoryCInertGasPercentage().setInputValue("50");
    form.getCategoryCHydrocarbonPercentage().setInputValue(hydrocarbonPercentage);
    form.setEvaluatedPerCategory(null);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("categoryAInertGasPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("categoryAHydrocarbonPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("categoryBInertGasPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("categoryBHydrocarbonPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("categoryCInertGasPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("categoryCHydrocarbonPercentage.inputValue", Collections.singletonList(INVALID_GAS_CONTENT_PERCENTAGE)),
        entry("evaluatedPerCategory", Collections.singletonList(EVALUATED_PER_CATEGORY_MISSING))
    );
  }

  @Test
  void validate_formWithEvaluatedPerCategoryQuestionUnanswered() {
    form.setEvaluatedPerCategory(null);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("evaluatedPerCategory", Collections.singletonList(EVALUATED_PER_CATEGORY_MISSING))
    );
  }

  @Test
  void validate_formWithEvaluatedPerCategoryExplanationNotProvided() {
    form.setEvaluatedPerCategory(false);
    form.setEvaluatedPerCategoryExplanation(null);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("evaluatedPerCategoryExplanation.inputValue",
            Collections.singletonList("Enter an explanation"))
    );
  }
}