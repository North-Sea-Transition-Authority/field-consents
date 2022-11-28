package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
class FlareAnnualFormValidator implements Validator {

  private final FlareAnnualMonthFormValidator flareAnnualMonthFormValidator;

  @Autowired
  public FlareAnnualFormValidator(FlareAnnualMonthFormValidator flareAnnualMonthFormValidator) {
    this.flareAnnualMonthFormValidator = flareAnnualMonthFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(FlareAnnualForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareAnnualForm form = (FlareAnnualForm) target;

    List<FlareAnnualMonthForm> flareAnnualMonthForms = form.getFlareAnnualMonthForms();

    // Validate each individual field for each flare annual month form
    for (int index = 0; index < flareAnnualMonthForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          flareAnnualMonthFormValidator,
          "flareAnnualMonthForms[" + index + "]",
          flareAnnualMonthForms.get(index),
          errors
      );
    }
  }
}
