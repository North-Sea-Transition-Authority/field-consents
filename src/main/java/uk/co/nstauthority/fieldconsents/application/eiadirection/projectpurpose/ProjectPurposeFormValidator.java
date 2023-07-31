package uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
class ProjectPurposeFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ProjectPurposeForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        "forPurposeOfEiaRegs",
        "required",
        "Select yes if this \"project\" is for the purposes of aforementioned regulations"
    );
  }
}
