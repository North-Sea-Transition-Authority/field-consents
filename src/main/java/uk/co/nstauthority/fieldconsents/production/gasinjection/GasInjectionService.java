package uk.co.nstauthority.fieldconsents.production.gasinjection;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@Service
public class GasInjectionService {

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

  public SummaryDataView getGasInjectionSummaryDataView(ApplicationVersion applicationVersion) {
    var willGasBeInjected = applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED)
        .orElse(null);

    return new SummaryDataView(
        List.of(SummaryKeyValue.fromBoolean(ApplicationFlagType.WILL_GAS_BE_INJECTED.getDisplayName(), willGasBeInjected)));
  }
}