package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.integer.IntegerInputValidator;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;

@Service
class FlareReportMonthFormValidator implements Validator {

  private final FlareVentRowFormValidator flareVentRowFormValidator;

  @Autowired
  FlareReportMonthFormValidator(FlareVentRowFormValidator flareVentRowFormValidator) {
    this.flareVentRowFormValidator = flareVentRowFormValidator;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareReportMonthForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareReportMonthForm monthForm = (FlareReportMonthForm) target;

    // validate the category data and comments
    ValidationUtils.invokeValidator(flareVentRowFormValidator, target, errors, errors);

    // the shutdown days must be greater than or equal to zero and less that or equal to the month days
    IntegerInputValidator.builder()
        .mustBeMoreThanOrEqualTo(0)
        .mustBeLessThanOrEqualTo(monthForm.getMonthDays())
        .validate(monthForm.getShutDownDays(), errors);
  }
}
