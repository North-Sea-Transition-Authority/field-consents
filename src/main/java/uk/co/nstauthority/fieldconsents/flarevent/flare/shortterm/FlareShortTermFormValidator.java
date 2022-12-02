package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
class FlareShortTermFormValidator implements Validator {

  private final FlareVentRowFormValidator flareVentRowFormValidator;

  @Autowired
  public FlareShortTermFormValidator(FlareVentRowFormValidator flareVentRowFormValidator) {
    this.flareVentRowFormValidator = flareVentRowFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(FlareShortTermForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareShortTermForm form = (FlareShortTermForm) target;

    List<FlareShortTermMonthForm> flareShortTermMonthForms = form.getFlareShortTermMonthForms();

    // Validate each individual field for each flare month form
    for (int index = 0; index < flareShortTermMonthForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          flareVentRowFormValidator,
          "flareShortTermMonthForms[" + index + "]",
          flareShortTermMonthForms.get(index),
          errors
      );
    }
  }
}
