package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.FieldConsentsPdfRenderResult;

@Service
public class ConsentDocumentGenerationDataService {

  private final ConsentDocumentGenerationDataRepository repository;

  ConsentDocumentGenerationDataService(ConsentDocumentGenerationDataRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void createDocumentGenerationData(
      Consent consent,
      DocumentInstanceDto documentInstanceDto,
      FieldConsentsPdfRenderResult fieldConsentsPdfRenderResult
  ) {
    var consentDocumentGenerationData = new ConsentDocumentGenerationData();

    consentDocumentGenerationData.setConsent(consent);
    consentDocumentGenerationData.setDocumentTemplateMnemonic(documentInstanceDto.documentTemplateDto().mnemonic());
    consentDocumentGenerationData.setDocumentTitle(documentInstanceDto.title());
    consentDocumentGenerationData.setPdfHtmlContent(fieldConsentsPdfRenderResult.pdfHtml());
    consentDocumentGenerationData.setMailMergeResolvedValuesByMnemonic(
        fieldConsentsPdfRenderResult.mailMergeResolvedValuesByMnemonic()
    );

    repository.save(consentDocumentGenerationData);
  }

}
