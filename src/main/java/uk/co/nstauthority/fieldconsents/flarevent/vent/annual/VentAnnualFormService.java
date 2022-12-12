package uk.co.nstauthority.fieldconsents.flarevent.vent.annual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
class VentAnnualFormService {

  private final VentAnnualFormValidator validator;

  @Autowired
  public VentAnnualFormService(VentAnnualFormValidator validator) {
    this.validator = validator;
  }

  BindingResult validate(VentAnnualForm form, BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    return bindingResult;
  }

}