package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
public class ApplicationUpdateResponseFormValidator implements Validator {

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return ApplicationUpdateResponseForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (ApplicationUpdateResponseForm) target;

    ValidationUtils.rejectIfEmpty(
        errors,
        "responseType",
        "required",
        "Select an option to describe the updates made"
    );

    if (ApplicationUpdateResponseType.OTHER_CHANGES.equals(form.responseType())) {
      StringInputValidator.builder()
          .validate(form.otherChangesDescription(), errors);
    }
  }
}
