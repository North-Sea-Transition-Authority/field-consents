package uk.co.nstauthority.fieldconsents.flarevent;

import java.math.BigDecimal;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.decimal.DecimalInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
public class FlareVentRowFormValidator implements Validator {

  public static final int COMMENTS_MAX_CHARACTER_COUNT = 300;

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareVentRowForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareVentRowForm monthForm = (FlareVentRowForm) target;

    // Each category field should have a non-empty number which can be greater or equal to 0.0
    // and must not contain more decimal places than specified by MAX_DECIMAL_PLACES
    var validator = DecimalInputValidator.builder()
        .mustBeMoreThanOrEqual(BigDecimal.ZERO)
        .mustHaveNoMoreThanDecimalPlaces(ValidatorUtils.MAX_DECIMAL_PLACES);

    validator.validate(monthForm.getCategoryA(), errors);
    validator.validate(monthForm.getCategoryB(), errors);
    validator.validate(monthForm.getCategoryC(), errors);

    // the comments are mandatory and must be no more than 300 chars long
    StringInputValidator.builder()
        .mustHaveCharacterCountAtMost(COMMENTS_MAX_CHARACTER_COUNT)
        .validate(monthForm.getComments(), errors);
  }
}
