package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;

@Service
class FlareAnnualMonthFormValidator implements Validator {

  public static final int COMMENTS_MAX_CHARACTER_COUNT = 300;

  private final FlareVentRowFormValidator flareVentRowFormValidator;

  @Autowired
  public FlareAnnualMonthFormValidator(FlareVentRowFormValidator flareVentRowFormValidator) {
    this.flareVentRowFormValidator = flareVentRowFormValidator;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareAnnualMonthForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareAnnualMonthForm monthForm = (FlareAnnualMonthForm) target;

    // validate the category data
    ValidationUtils.invokeValidator(flareVentRowFormValidator, target, errors, errors);

    // the comments are mandatory and must be no more than 300 chars long
    StringInputValidator.builder()
        .mustHaveCharacterCountAtMost(COMMENTS_MAX_CHARACTER_COUNT)
        .validate(monthForm.getComments(), errors);
  }

}
