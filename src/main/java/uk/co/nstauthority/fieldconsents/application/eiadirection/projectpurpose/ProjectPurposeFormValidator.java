package uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Component
class ProjectPurposeFormValidator {

  public void validate(ProjectPurposeForm form, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        "forPurposeOfEiaRegs",
        "required",
        "Select yes if this \"project\" is for the purposes of aforementioned regulations"
    );

    if (Boolean.TRUE.equals(form.forPurposeOfEiaRegs())) {
      StringInputValidator.builder()
          .validate(form.rationaleForPurposeOfEiaRegs(), errors);
    } else if (Boolean.FALSE.equals(form.forPurposeOfEiaRegs())) {
      StringInputValidator.builder()
          .validate(form.rationaleNotForPurposeOfEiaRegs(), errors);
    }
  }
}
