package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.PdfRenderResultWithGenerationData;

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
      PdfRenderResultWithGenerationData pdfRenderResultWithGenerationData
  ) {
    var consentDocumentGenerationData = new ConsentDocumentGenerationData();

    consentDocumentGenerationData.setConsent(consent);
    consentDocumentGenerationData.setDocumentTemplateMnemonic(documentInstanceDto.documentTemplateDto().mnemonic());
    consentDocumentGenerationData.setDocumentTitle(documentInstanceDto.title());
    consentDocumentGenerationData.setPdfHtmlContent(pdfRenderResultWithGenerationData.pdfRenderResult().pdfHtml());
    consentDocumentGenerationData.setMailMergeResolvedValuesByMnemonic(
        pdfRenderResultWithGenerationData.mailMergeResolvedValuesByMnemonic()
    );

    repository.save(consentDocumentGenerationData);
  }

}
