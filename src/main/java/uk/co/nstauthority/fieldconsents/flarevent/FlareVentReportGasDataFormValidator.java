package uk.co.nstauthority.fieldconsents.flarevent;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.fivium.formlibrary.validator.decimal.DecimalInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
public class FlareVentReportGasDataFormValidator implements Validator {

  public static final String FIELD_INPUT_VALUE = ".inputValue";

  public static final String EVALUATED_PER_CATEGORY_MISSING =
      "Select yes if you have evaluated the gas properties for each category individually";

  public static final String INVALID_GAS_CONTENT_PERCENTAGE =
      "The inert gas content and hydrocarbon gas content should be 100% in total for each category";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareVentReportGasDataForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareVentReportGasDataForm form = (FlareVentReportGasDataForm) target;

    // Each category field for standard density should have a non-empty number which can be greater or equal to 0.0
    // with a maximum of 6 decimal places
    var densityValidator = DecimalInputValidator.builder()
        .mustBeMoreThanOrEqualTo(BigDecimal.ZERO)
        .mustHaveNoMoreThanDecimalPlaces(ValidatorUtils.MAX_DECIMAL_PLACES);

    densityValidator.validate(form.getCategoryADensity(), errors);
    densityValidator.validate(form.getCategoryBDensity(), errors);
    densityValidator.validate(form.getCategoryCDensity(), errors);

    // Each category field for gas content (Inert and Hydrocarbon) should have a non-empty number which can be between 0.0
    // and 100.0 (as this indicates a percentage) with a maximum of 6 decimal places
    var percentageValidator = DecimalInputValidator.builder()
        .mustBeMoreThanOrEqualTo(BigDecimal.ZERO)
        .mustBeLessThanOrEqualTo(BigDecimal.valueOf(100))
        .mustHaveNoMoreThanDecimalPlaces(ValidatorUtils.MAX_DECIMAL_PLACES);

    percentageValidator.validate(form.getCategoryAInertGasPercentage(), errors);
    percentageValidator.validate(form.getCategoryAHydrocarbonPercentage(), errors);

    percentageValidator.validate(form.getCategoryBInertGasPercentage(), errors);
    percentageValidator.validate(form.getCategoryBHydrocarbonPercentage(), errors);

    percentageValidator.validate(form.getCategoryCInertGasPercentage(), errors);
    percentageValidator.validate(form.getCategoryCHydrocarbonPercentage(), errors);

    // The Inert gas and Hydrocarbon content values should add up to exactly 100% for each category
    if (!errors.hasFieldErrors(form.getCategoryAInertGasPercentage().getFieldName() + FIELD_INPUT_VALUE)
        && !errors.hasFieldErrors(form.getCategoryAHydrocarbonPercentage().getFieldName() + FIELD_INPUT_VALUE)) {
      validateGasPercentagePerCategory(errors, form.getCategoryAInertGasPercentage(), form.getCategoryAHydrocarbonPercentage());
    }
    
    if (!errors.hasFieldErrors(form.getCategoryBInertGasPercentage().getFieldName() + FIELD_INPUT_VALUE)
        && !errors.hasFieldErrors(form.getCategoryBHydrocarbonPercentage().getFieldName() + FIELD_INPUT_VALUE)) {
      validateGasPercentagePerCategory(errors, form.getCategoryBInertGasPercentage(), form.getCategoryBHydrocarbonPercentage());
    }

    if (!errors.hasFieldErrors(form.getCategoryCInertGasPercentage().getFieldName() + FIELD_INPUT_VALUE)
        && !errors.hasFieldErrors(form.getCategoryCHydrocarbonPercentage().getFieldName() + FIELD_INPUT_VALUE)) {
      validateGasPercentagePerCategory(errors, form.getCategoryCInertGasPercentage(), form.getCategoryCHydrocarbonPercentage());
    }

    ValidationUtils.rejectIfEmpty(errors, "evaluatedPerCategory", "evaluatedPerCategory.required",
        EVALUATED_PER_CATEGORY_MISSING);
    if (Boolean.FALSE.equals(form.getEvaluatedPerCategory())) {
      StringInputValidator.builder()
          .validate(form.getEvaluatedPerCategoryExplanation(), errors);
    }
  }

  private void validateGasPercentagePerCategory(Errors errors, DecimalInput categoryInertGasDecimalInput,
                                                DecimalInput categoryHydrocarbonDecimalInput) {
    BigDecimal categoryInertGas = categoryInertGasDecimalInput.getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);
    BigDecimal categoryHydrocarbon = categoryHydrocarbonDecimalInput.getAsBigDecimal()
        .orElseThrow(NoSuchElementException::new);

    if (categoryInertGas.add(categoryHydrocarbon).compareTo(BigDecimal.valueOf(100)) != 0) {
      errors.rejectValue(
          categoryInertGasDecimalInput.getFieldName() + FIELD_INPUT_VALUE,
          categoryInertGasDecimalInput.getFieldName() + ".invalid",
          INVALID_GAS_CONTENT_PERCENTAGE
      );
      errors.rejectValue(
          categoryHydrocarbonDecimalInput.getFieldName() + FIELD_INPUT_VALUE,
          categoryHydrocarbonDecimalInput.getFieldName() + ".invalid",
          ""
      );
    }
  }
}
