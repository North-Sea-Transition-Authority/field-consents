package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

@Order(0)
@Component
class ApplicationReferenceMailMergeField implements DocumentMailMergeField {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ApplicationService applicationService;

  @Autowired
  ApplicationReferenceMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ApplicationService applicationService
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.applicationService = applicationService;
  }

  @Override
  public String getMnemonic() {
    return "APPLICATION_REFERENCE";
  }

  @Override
  public String getDescription() {
    return "The reference assigned to the application";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    return applicationService.generateApplicationReference(applicationVersion);
  }
}
