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
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Order(DocumentMailMergeFieldDisplayOrders.SUPERSEDED_CONSENT_ISSUE_DATE)
@Component
class SupersededConsentIssueDateMailMergeField implements DocumentMailMergeField {

  private static final String MNEMONIC = "SUPERSEDED_CONSENT_ISSUE_DATE";
  private static final String DESCRIPTION = "The date the superseded consent was issued (revisions only)";

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final ConsentService consentService;

  SupersededConsentIssueDateMailMergeField(
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
    var applicationId = applicationDocumentInstanceLinkingService
        .getApplicationIdFromDocumentInstanceDtoOrThrowIfInvalidItemType(documentInstanceDto);

    return consentService.findPreviousConsentByApplicationId(applicationId)
        .map(previousConsent -> {
          var previousConsentIssuedInstant = previousConsent.getIssuedInstant();

          return DocumentMailMergeFieldResolveResult.success(DateUtils.format(previousConsentIssuedInstant, DateUtils.LONG_DATE));
        })
        .orElse(DocumentMailMergeFieldResolveResult.error("Mail merge field %s is not valid. Application is not a revision"
            .formatted(MNEMONIC)));
  }
}
