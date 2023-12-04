package uk.co.nstauthority.fieldconsents.document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;

@Service
class DocumentTemplateBootstrapService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentTemplateBootstrapService.class);

  private final DocumentTemplateService documentTemplateService;
  private final DocumentTemplateSectionService documentTemplateSectionService;

  @Autowired
  DocumentTemplateBootstrapService(
      DocumentTemplateService documentTemplateService,
      DocumentTemplateSectionService documentTemplateSectionService
  ) {
    this.documentTemplateService = documentTemplateService;
    this.documentTemplateSectionService = documentTemplateSectionService;
  }

  @EventListener(ApplicationReadyEvent.class)
  void onApplicationReadyEvent() {
    if (!documentTemplateService.getDocumentTemplateDtos().isEmpty()) {
      LOGGER.info("Found existing document templates, not creating initial document templates");
      return;
    }

    LOGGER.info("Creating initial document templates");

    var productionDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        "Production Consent",
        "Document template used for creating Production Consents",
        "document/template/productionConsent.ftl",
        1
    );

    var exampleContent = "Example content\nwith multiple lines";

    documentTemplateSectionService.createDocumentTemplateSection(
        productionDocumentTemplateDto,
        null,
        "First section",
        exampleContent,
        1
    );

    var secondSection = documentTemplateSectionService.createDocumentTemplateSection(
        productionDocumentTemplateDto,
        null,
        "Second section",
        exampleContent,
        2
    );

    var secondSectionFirstSubsection = documentTemplateSectionService.createDocumentTemplateSection(
        productionDocumentTemplateDto,
        secondSection,
        "Second section first subsection",
        exampleContent,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        productionDocumentTemplateDto,
        secondSectionFirstSubsection,
        "Second section first subsection first subsection",
        exampleContent,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        productionDocumentTemplateDto,
        secondSection,
        "Second section second subsection",
        exampleContent,
        2
    );

    documentTemplateService.createDocumentTemplate(
        "Flare Consent",
        "Document template used for creating Flare Consents",
        "document/template/flareConsent.ftl",
        2
    );
    documentTemplateService.createDocumentTemplate(
        "Vent Consent",
        "Document template used for creating Vent Consents",
        "document/template/ventConsent.ftl",
        3
    );
  }
}
