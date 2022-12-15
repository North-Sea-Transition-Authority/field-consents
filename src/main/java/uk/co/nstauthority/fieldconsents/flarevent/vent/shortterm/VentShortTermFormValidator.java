package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
class VentShortTermFormValidator implements Validator {

  private final FlareVentRowFormValidator flareVentRowFormValidator;

  @Autowired
  public VentShortTermFormValidator(FlareVentRowFormValidator flareVentRowFormValidator) {
    this.flareVentRowFormValidator = flareVentRowFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(VentShortTermForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    VentShortTermForm form = (VentShortTermForm) target;

    List<VentShortTermMonthForm> ventShortTermMonthForms = form.getVentShortTermMonthForms();

    // Validate each individual field for each vent month form
    for (int index = 0; index < ventShortTermMonthForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          flareVentRowFormValidator,
          "ventShortTermMonthForms[" + index + "]",
          ventShortTermMonthForms.get(index),
          errors
      );
    }
  }
}
