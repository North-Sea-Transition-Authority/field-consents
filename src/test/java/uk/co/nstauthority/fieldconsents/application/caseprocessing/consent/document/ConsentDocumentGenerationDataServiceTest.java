package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.FieldConsentsPdfRenderResult;

@ExtendWith(MockitoExtension.class)
class ConsentDocumentGenerationDataServiceTest {

  @Mock
  private ConsentDocumentGenerationDataRepository repository;

  @InjectMocks
  private ConsentDocumentGenerationDataService consentDocumentGenerationDataService;

  @Captor
  private ArgumentCaptor<ConsentDocumentGenerationData> consentDocumentGenerationDataCaptor;

  @Test
  void createDocumentGenerationData() {
    var consent = new Consent();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var pdfRenderResultWithGenerationData = new FieldConsentsPdfRenderResult(
            new ByteArrayResource(new byte[]{1, 2, 3}),
            "<html/>",
        Map.of("KEY", "VALUE")
    );

    consentDocumentGenerationDataService.createDocumentGenerationData(consent, documentInstanceDto,
        pdfRenderResultWithGenerationData);

    verify(repository).save(consentDocumentGenerationDataCaptor.capture());

    assertThat(consentDocumentGenerationDataCaptor.getValue())
        .extracting(
            ConsentDocumentGenerationData::getConsent,
            ConsentDocumentGenerationData::getDocumentTemplateMnemonic,
            ConsentDocumentGenerationData::getDocumentTitle,
            ConsentDocumentGenerationData::getPdfHtmlContent,
            ConsentDocumentGenerationData::getMailMergeResolvedValuesByMnemonic
        ).containsExactly(
            consent,
            documentInstanceDto.documentTemplateDto().mnemonic(),
            documentInstanceDto.title(),
            pdfRenderResultWithGenerationData.pdfHtml(),
            pdfRenderResultWithGenerationData.mailMergeResolvedValuesByMnemonic()
        );
  }
}
