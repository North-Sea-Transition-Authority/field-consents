package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service
public class VentSetupFormValidator implements Validator {

  public static final String HAS_OTHER_VENTS_TO_ADD_EMPTY = "Select whether you have more vents to add";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return VentSetupForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {

    ValidationUtils.rejectIfEmpty(errors, "hasOtherVentsToAdd", "hasOtherVentsToAdd.required",
        HAS_OTHER_VENTS_TO_ADD_EMPTY);

  }
}
