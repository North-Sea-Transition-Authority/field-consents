package uk.co.nstauthority.fieldconsents.document.templatesectioncondition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionCondition;

@Component
class ApplicationIsLongTermCondition implements DocumentTemplateSectionCondition {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ConsentLengthService consentLengthService;

  @Autowired
  ApplicationIsLongTermCondition(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ConsentLengthService consentLengthService
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.consentLengthService = consentLengthService;
  }

  @Override
  public String getMnemonic() {
    return "APPLICATION_IS_LONG_TERM";
  }

  @Override
  public String getTitle() {
    return "Application is long term";
  }

  @Override
  public boolean evaluate(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getApplicationVersionFromDocumentInstanceDto(documentInstanceDto);
    var consentLength = consentLengthService.getConsentLengthDetails(applicationVersion);

    return consentLength.getConsentLength().equals(ConsentLengthType.LONG_TERM);
  }
}
