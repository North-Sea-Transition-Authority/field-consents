package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service
class CaseAssignmentFormValidator implements Validator {

  static final String CASE_OFFICER_EMPTY = "Select a case officer";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return CaseAssignmentForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "caseOfficerWuaId", "required", CASE_OFFICER_EMPTY);
  }
}
