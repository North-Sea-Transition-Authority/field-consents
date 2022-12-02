package uk.co.nstauthority.fieldconsents.flarevent.flare.shortterm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
class FlareShortTermFormService {

  private final FlareShortTermFormValidator validator;

  @Autowired
  public FlareShortTermFormService(FlareShortTermFormValidator validator) {
    this.validator = validator;
  }

  BindingResult validate(FlareShortTermForm form, BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    return bindingResult;
  }

}