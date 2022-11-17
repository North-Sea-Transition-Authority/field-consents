package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
class FlareReportFormService {

  private final FlareReportFormValidator validator;

  @Autowired
  FlareReportFormService(FlareReportFormValidator validator) {
    this.validator = validator;
  }

  BindingResult validate(FlareReportForm form, BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    return bindingResult;
  }

}

