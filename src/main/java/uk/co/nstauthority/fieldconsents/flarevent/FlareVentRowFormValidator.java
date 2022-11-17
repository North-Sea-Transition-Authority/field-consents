package uk.co.nstauthority.fieldconsents.flarevent;

import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.decimal.DecimalInputValidator;

@Service
public class FlareVentRowFormValidator implements Validator {

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareVentRowForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareVentRowForm monthForm = (FlareVentRowForm) target;

    // Each form field should have a non-empty number which can be greater or equal to 0.0
    var validator = DecimalInputValidator.builder()
        .mustBeMoreThanOrEqual(BigDecimal.ZERO);

    validator.validate(monthForm.getCategoryA(), errors);
    validator.validate(monthForm.getCategoryB(), errors);
    validator.validate(monthForm.getCategoryC(), errors);
  }
}
