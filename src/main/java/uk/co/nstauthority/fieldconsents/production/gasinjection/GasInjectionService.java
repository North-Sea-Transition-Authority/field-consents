package uk.co.nstauthority.fieldconsents.production.gasinjection;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
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

  public SummaryCard getGasInjectionSummaryCard(ApplicationVersion applicationVersion) {
    var willGasBeInjectedOptional = applicationFlagService
        .findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED);

    if (willGasBeInjectedOptional.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    return SummaryCard.simpleSummaryCard(
        List.of(SummaryKeyValue.fromBoolean(ApplicationFlagType.WILL_GAS_BE_INJECTED.getDisplayName(),
            willGasBeInjectedOptional.get()))
    );
  }
}