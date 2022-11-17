package uk.co.nstauthority.fieldconsents.production;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.decimal.DecimalInputValidator;

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
    var validator = DecimalInputValidator.builder()
        .mustBeMoreThanOrEqual(BigDecimal.ZERO);

    validator.validate(monthForm.getOilMinValue(), errors);
    validator.validate(monthForm.getOilMaxValue(), errors);
    validator.validate(monthForm.getGasMinValue(), errors);
    validator.validate(monthForm.getGasMaxValue(), errors);

    // The values for min oil (and gas) must not exceed the corresponding values for max oil (and gas)
    if (!errors.hasErrors()) {
      try {
        var validatorOilMinMax = DecimalInputValidator.builder()
            .mustBeBetween(BigDecimal.ZERO, monthForm.getOilMaxValue().getAsBigDecimal()
                .orElseThrow(NoSuchElementException::new)
            );
        validatorOilMinMax.validate(monthForm.getOilMinValue(), errors);

        var validatorGasMinMax = DecimalInputValidator.builder()
            .mustBeBetween(BigDecimal.ZERO, monthForm.getGasMaxValue().getAsBigDecimal()
                .orElseThrow(NoSuchElementException::new)
            );
        validatorGasMinMax.validate(monthForm.getGasMinValue(), errors);
      } catch (NoSuchElementException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
