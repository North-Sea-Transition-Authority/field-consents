package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

@Order(5)
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
    return DocumentTemplateType.isConsent(DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic()));
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);
    var consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

    return consentLengthDetails.getConsentLength().getShortDisplayName().toUpperCase();
  }
}
