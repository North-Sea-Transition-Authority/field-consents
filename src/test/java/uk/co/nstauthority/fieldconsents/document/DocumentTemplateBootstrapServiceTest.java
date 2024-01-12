package uk.co.nstauthority.fieldconsents.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateBootstrapServiceTest {

  @Mock
  private DocumentTemplateService documentTemplateService;

  @Mock
  private DocumentTemplateSectionService documentTemplateSectionService;

  @InjectMocks
  private DocumentTemplateBootstrapService documentTemplateBootstrapService;

  @Test
  void onApplicationReadyEvent_existingDocumentTemplateDtos() {
    when(documentTemplateService.getDocumentTemplateDtos())
        .thenReturn(List.of(DocumentTemplateDtoTestUtil.builder().build()));

    documentTemplateBootstrapService.onApplicationReadyEvent();

    verify(documentTemplateService, never()).createDocumentTemplate(any(), any(), any(), any(), anyInt());
    verify(documentTemplateSectionService, never())
        .createDocumentTemplateSection(any(), any(), any(), any(), any(), anyBoolean(), anyInt());
  }

  @Test
  void onApplicationReadyEvent_noExistingDocumentTemplateDtos() {
    var productionDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var secondSection = DocumentTemplateSectionDtoTestUtil.builder().build();
    var secondSectionFirstSubsection = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(documentTemplateService.getDocumentTemplateDtos()).thenReturn(List.of());

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.PRODUCTION_CONSENT.getMnemonic(),
            "Production Consent",
            "Document template used for creating Production Consents",
            "fcs/document/template/productionConsent.ftl",
            1
        )
    ).thenReturn(productionDocumentTemplateDto);

    when(
        documentTemplateSectionService.createDocumentTemplateSection(
            any(),
            any(),
            any(),
            any(),
            any(),
            anyBoolean(),
            anyInt()
        )
    ).thenReturn(null, secondSection, secondSectionFirstSubsection);

    documentTemplateBootstrapService.onApplicationReadyEvent();

    verify(documentTemplateService).createDocumentTemplate(
        DocumentTemplateType.PRODUCTION_CONSENT.getMnemonic(),
        "Production Consent",
        "Document template used for creating Production Consents",
        "fcs/document/template/productionConsent.ftl",
        1
    );

    var exampleContent = "Example content\nwith multiple lines";

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        productionDocumentTemplateDto,
        null,
        "First section",
        exampleContent,
        null,
        true,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        productionDocumentTemplateDto,
        null,
        "Second section",
        exampleContent,
        null,
        true,
        2
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        productionDocumentTemplateDto,
        secondSection,
        "Second section first subsection",
        exampleContent,
        null,
        true,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        productionDocumentTemplateDto,
        secondSectionFirstSubsection,
        "Second section first subsection first subsection",
        exampleContent,
        null,
        true,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        productionDocumentTemplateDto,
        secondSection,
        "Second section second subsection",
        exampleContent,
        null,
        true,
        2
    );

    verify(documentTemplateService).createDocumentTemplate(
        DocumentTemplateType.FLARE_CONSENT.getMnemonic(),
        "Flare Consent",
        "Document template used for creating Flare Consents",
        "fcs/document/template/flareConsent.ftl",
        2
    );
    verify(documentTemplateService).createDocumentTemplate(
        DocumentTemplateType.VENT_CONSENT.getMnemonic(),
        "Vent Consent",
        "Document template used for creating Vent Consents",
        "fcs/document/template/ventConsent.ftl",
        3
    );
  }
}
