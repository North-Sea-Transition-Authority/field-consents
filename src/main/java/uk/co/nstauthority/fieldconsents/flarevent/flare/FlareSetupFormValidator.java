package uk.co.nstauthority.fieldconsents.flarevent.flare;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service
public class FlareSetupFormValidator implements Validator {

  public static final String HAS_OTHER_FLARES_TO_ADD_EMPTY = "Select whether you have more flares to add";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareSetupForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {

    ValidationUtils.rejectIfEmpty(errors, "hasOtherFlaresToAdd", "hasOtherFlaresToAdd.required",
        HAS_OTHER_FLARES_TO_ADD_EMPTY);

  }
}
