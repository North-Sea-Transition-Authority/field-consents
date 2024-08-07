package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
public class ConsentBreachFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ConsentBreachForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (ConsentBreachForm) target;

    StringInputValidator.builder()
        .validate(form.getConsentBreachText(), errors);
  }
}
