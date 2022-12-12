package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
class VentAnnualFormValidator implements Validator {

  private final FlareVentRowFormValidator flareVentRowFormValidator;

  @Autowired
  public VentAnnualFormValidator(FlareVentRowFormValidator flareVentRowFormValidator) {
    this.flareVentRowFormValidator = flareVentRowFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(VentAnnualForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    VentAnnualForm form = (VentAnnualForm) target;

    List<VentAnnualMonthForm> ventAnnualMonthForms = form.getVentAnnualMonthForms();

    // Validate each individual field for each vent annual month form
    for (int index = 0; index < ventAnnualMonthForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          flareVentRowFormValidator,
          "ventAnnualMonthForms[" + index + "]",
          ventAnnualMonthForms.get(index),
          errors
      );
    }
  }
}
