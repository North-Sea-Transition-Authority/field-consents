package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Service
class TechnicalReviewAssignmentFormValidator implements Validator {

  static final String TECHNICAL_REVIEWER_EMPTY = "Select a technical reviewer";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return TechnicalReviewAssignmentForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "technicalReviewerWuaId", "required", TECHNICAL_REVIEWER_EMPTY);
  }
}
