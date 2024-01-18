package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;

@Service
class ConsentDataFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ConsentDataForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (ConsentDataForm) target;

    var consentStartDateInput = form.consentStartDate();
    var consentStartDateOptional = consentStartDateInput.getAsLocalDate();

    ThreeFieldDateInputValidator.builder().validate(consentStartDateInput, errors);

    if (consentStartDateOptional.isEmpty() || errors.hasErrors()) {
      return;
    }

    var consentStartDate = consentStartDateOptional.get();

    ThreeFieldDateInputValidator.builder()
        .mustBeAfterOrEqualTo(consentStartDate)
        .mustBeAfterOrEqualToErrorMessage("Consent end date must be on or after the consent start date")
        .validate(form.consentEndDate(), errors);
  }
}
