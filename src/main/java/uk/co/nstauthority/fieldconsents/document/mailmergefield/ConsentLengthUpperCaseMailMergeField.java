package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;

@Order(7)
@Component
class ConsentLengthUpperCaseMailMergeField implements DocumentMailMergeField {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ConsentLengthService consentLengthService;

  @Autowired
  ConsentLengthUpperCaseMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ConsentLengthService consentLengthService
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.consentLengthService = consentLengthService;
  }

  @Override
  public String getMnemonic() {
    return "CONSENT_LENGTH_UPPER_CASE";
  }

  @Override
  public String getDescription() {
    return "The length of the Consent in upper case";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);
    var consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);
    var shortDisplayName = consentLengthDetails.getConsentLength().getShortDisplayName();

    return DocumentMailMergeFieldResolveResult.success(shortDisplayName.toUpperCase());
  }
}
