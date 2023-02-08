package uk.co.nstauthority.fieldconsents.production.gasinjection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;

@Service
class GasInjectionService {

  private final ApplicationFlagService applicationFlagService;

  @Autowired
  GasInjectionService(ApplicationFlagService applicationFlagService) {
    this.applicationFlagService = applicationFlagService;
  }

  public GasInjectionForm getGasInjectionForm(ApplicationVersion applicationVersion) {
    GasInjectionForm gasInjectionForm = new GasInjectionForm();
    applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED)
        .ifPresent(gasInjectionForm::setWillGasBeInjected);

    return gasInjectionForm;
  }
}