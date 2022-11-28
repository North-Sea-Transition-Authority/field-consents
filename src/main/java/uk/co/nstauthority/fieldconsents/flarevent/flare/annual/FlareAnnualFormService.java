package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
class FlareAnnualFormService {

  private final FlareAnnualFormValidator validator;

  @Autowired
  public FlareAnnualFormService(FlareAnnualFormValidator validator) {
    this.validator = validator;
  }

  BindingResult validate(FlareAnnualForm form, BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    return bindingResult;
  }

}