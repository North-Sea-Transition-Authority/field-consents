package uk.co.nstauthority.fieldconsents.document.template.sectioncondition;

import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionCondition;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.revision.ApplicationRevisionType;

@Component
class ApplicationIsRevisionCondition implements DocumentTemplateSectionCondition {

  private static final String MNEMONIC = "APPLICATION_IS_REVISION";
  private static final String TITLE = "Application is revision";

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  ApplicationIsRevisionCondition(ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
  }

  @Override
  public String getMnemonic() {
    return MNEMONIC;
  }

  @Override
  public String getTitle() {
    return TITLE;
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public boolean evaluate(DocumentInstanceDto documentInstanceDto) {
    var application = applicationDocumentInstanceLinkingService.getApplicationFromDocumentInstanceDto(documentInstanceDto);

    return ApplicationRevisionType.from(application) == ApplicationRevisionType.REVISION;
  }
}
