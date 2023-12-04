package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service
public class CamAssignmentFormValidator implements Validator {

  static final String CAM_EMPTY = "Select a consents and authorisations manager";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return CamAssignmentForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "camWuaId", "required", CAM_EMPTY);
  }
}
