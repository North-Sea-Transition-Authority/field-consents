package uk.co.nstauthority.fieldconsents.document.templatesectioncondition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionCondition;

@Component
class GasWillBeInjectedCondition implements DocumentTemplateSectionCondition {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ApplicationFlagService applicationFlagService;

  @Autowired
  GasWillBeInjectedCondition(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ApplicationFlagService applicationFlagService
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.applicationFlagService = applicationFlagService;
  }

  @Override
  public String getMnemonic() {
    return "GAS_WILL_BE_INJECTED";
  }

  @Override
  public String getTitle() {
    return "Gas will be injected";
  }

  @Override
  public boolean evaluate(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);
    return applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED)
        .orElse(false);
  }
}
