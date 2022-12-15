package uk.co.nstauthority.fieldconsents.flarevent.vent.shortterm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
class VentShortTermFormService {

  private final VentShortTermFormValidator validator;

  @Autowired
  public VentShortTermFormService(
      VentShortTermFormValidator validator) {
    this.validator = validator;
  }

  BindingResult validate(VentShortTermForm form, BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    return bindingResult;
  }

}