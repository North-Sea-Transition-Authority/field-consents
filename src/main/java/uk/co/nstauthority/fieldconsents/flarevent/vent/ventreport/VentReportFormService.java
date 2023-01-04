package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Service
class VentReportFormService {

  private final VentReportFormValidator validator;

  @Autowired
  VentReportFormService(VentReportFormValidator validator) {
    this.validator = validator;
  }

  BindingResult validate(VentReportForm form, BindingResult bindingResult) {
    validator.validate(form, bindingResult);
    return bindingResult;
  }
}

