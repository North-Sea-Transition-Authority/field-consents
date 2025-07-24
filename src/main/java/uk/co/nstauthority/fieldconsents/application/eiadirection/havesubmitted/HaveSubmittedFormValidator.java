package uk.co.nstauthority.fieldconsents.application.eiadirection.havesubmitted;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;

@Component
public class HaveSubmittedFormValidator implements Validator {

  private final PetsApplicationService petsApplicationService;

  HaveSubmittedFormValidator(PetsApplicationService petsApplicationService) {
    this.petsApplicationService = petsApplicationService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return HaveSubmittedForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        "haveSubmittedEiaDirection",
        "required",
        "Select yes if you have submitted an EIA screening direction"
    );

    if (errors.hasErrors()) {
      return;
    }

    // If an EIA has not been submitted, there won't be an satId to validate
    var form = (HaveSubmittedForm) target;
    if (!Boolean.TRUE.equals(form.haveSubmittedEiaDirection())) {
      return;
    }

    ValidationUtils.rejectIfEmpty(errors, "satId", "required", "Select an EIA screening direction reference");

    if (errors.hasErrors()) {
      return;
    }

    var petsApplicationOptional = petsApplicationService.findEiaDirectionById(
        form.satId(),
        "EIA direction form validation"
    );
    if (petsApplicationOptional.isEmpty()) {
      errors.rejectValue("satId", "invalid", "That EIA screening direction does not exist");
    }
  }
}
