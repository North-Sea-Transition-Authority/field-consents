package uk.co.nstauthority.fieldconsents.production;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.decimal.DecimalInputValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
public class ProductionRowFormValidator implements Validator {

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return ProductionRowForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    ProductionRowForm monthForm = (ProductionRowForm) target;

    // Each form field should have a non-empty double which can be greater or equal to 0.0
    // and must not contain more decimal places than specified by MAX_DECIMAL_PLACES
    var validator = DecimalInputValidator.builder()
        .mustBeMoreThanOrEqualTo(BigDecimal.ZERO)
        .mustHaveNoMoreThanDecimalPlaces(ValidatorUtils.MAX_DECIMAL_PLACES);

    validator.validate(monthForm.getOilMinValue(), errors);
    validator.validate(monthForm.getOilMaxValue(), errors);
    validator.validate(monthForm.getGasMinValue(), errors);
    validator.validate(monthForm.getGasMaxValue(), errors);

    // if no oil errors validate that the max is more than or equal to the min
    if (!errors.hasFieldErrors("oilMinValue.inputValue")
        && !errors.hasFieldErrors("oilMaxValue.inputValue")) {
      DecimalInputValidator.builder()
          .mustBeMoreThanOrEqualTo(
              monthForm.getOilMinValue().getAsBigDecimal()
                  .orElseThrow(NoSuchElementException::new))
          .validate(monthForm.getOilMaxValue(), errors);
    }

    // if no gas errors validate that the max is more than or equal to the min
    if (!errors.hasFieldErrors("gasMinValue.inputValue")
        && !errors.hasFieldErrors("gasMaxValue.inputValue")) {
      DecimalInputValidator.builder()
          .mustBeMoreThanOrEqualTo(
              monthForm.getGasMinValue().getAsBigDecimal()
                  .orElseThrow(NoSuchElementException::new))
          .validate(monthForm.getGasMaxValue(), errors);
    }
  }
}
