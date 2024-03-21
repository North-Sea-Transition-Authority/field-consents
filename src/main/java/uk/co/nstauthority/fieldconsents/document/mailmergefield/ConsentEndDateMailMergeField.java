package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataRepository;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Order(6)
@Component
public class ConsentEndDateMailMergeField implements DocumentMailMergeField {

  private static final String MNEMONIC = "CONSENT_END_DATE";
  private static final String DESCRIPTION = "The Consent end date for this application";

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final ConsentDataRepository repository;

  ConsentEndDateMailMergeField(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      ConsentDataRepository repository
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.repository = repository;
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
    return true;
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var application = applicationDocumentInstanceLinkingService.getApplicationFromDocumentInstanceDto(documentInstanceDto);
    return repository.findByApplication(application)
        .map(ConsentData::getConsentEndDate)
        .map(date -> DateUtils.format(date, DateUtils.LONG_DATE))
        .map(DocumentMailMergeFieldResolveResult::success)
        .orElseThrow(() -> MailMergeFieldFailedToResolveException
            .mnemonicDoesNotExistOnDocumentInstance(MNEMONIC, documentInstanceDto));
  }
}
