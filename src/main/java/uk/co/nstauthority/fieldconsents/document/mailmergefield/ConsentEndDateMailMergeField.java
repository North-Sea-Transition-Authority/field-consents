package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentData;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataRepository;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Component
public class ConsentEndDateMailMergeField implements DocumentMailMergeField {

  private static final String MNEMONIC = "CONSENT_END_DATE";
  private static final String DESCRIPTION = "The Consent end date for this application";

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ConsentDataRepository repository;

  ConsentEndDateMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ConsentDataRepository repository
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
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
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var application = documentInstanceLinkingService.getApplicationFromDocumentInstanceDto(documentInstanceDto);
    return repository.findByApplication(application)
        .map(ConsentData::getConsentEndDate)
        .map(date -> DateUtils.format(date, DateUtils.LONG_DATE))
        .orElseThrow(() -> MailMergeFieldFailedToResolveException
            .mnemonicDoesNotExistOnDocumentInstance(MNEMONIC, documentInstanceDto));
  }
}
