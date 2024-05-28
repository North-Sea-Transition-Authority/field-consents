package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@Order(DocumentMailMergeFieldDisplayOrders.SUPERSEDED_CONSENT_REFERENCE)
@Component
class SupersededConsentReferenceMailMergeField implements DocumentMailMergeField {

  private static final String MNEMONIC = "SUPERSEDED_CONSENT_REFERENCE";
  private static final String DESCRIPTION = "The superseded consent's reference (revisions only)";

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final ConsentService consentService;

  SupersededConsentReferenceMailMergeField(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      ConsentService consentService
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.consentService = consentService;
  }

  @Override
  public String getMnemonic() {
    return MNEMONIC;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic()).isConsent();
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var application = applicationDocumentInstanceLinkingService.getApplicationFromDocumentInstanceDto(documentInstanceDto);

    if (!application.isRevision()) {
      return DocumentMailMergeFieldResolveResult.error("Mail merge field %s is not valid. Application is not a revision"
          .formatted(MNEMONIC));
    }

    var previousConsent = consentService.getPreviousConsent(application);
    var previousConsentApplicationReference = consentService.generateConsentApplicationReference(previousConsent);

    return DocumentMailMergeFieldResolveResult.success(previousConsentApplicationReference);
  }
}
