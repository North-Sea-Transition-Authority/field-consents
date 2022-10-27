package uk.co.nstauthority.fieldconsents.startapplication;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service
public class StartApplicationFormValidator implements Validator {

  private static final String APPLICATION_TYPE_EMPTY = "Select the application type";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return StartApplicationForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "applicationType", "required", APPLICATION_TYPE_EMPTY);
  }
}
