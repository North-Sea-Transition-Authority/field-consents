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

    createFieldProductionConsentDocumentTemplate();
    createFieldFlareConsentDocumentTemplate();
    createFieldVentConsentDocumentTemplate();
  }

  void createFieldProductionConsentDocumentTemplate() {
    var fieldProductionConsentDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic(),
        "Production Consent",
        "Document template used for creating Production Consents",
        "fcs/document/template/consent/production/fieldProductionConsent.ftl",
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Header",
        """
        PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)
        ((CONSENT_LENGTH_UPPER_CASE)) DEVELOPMENT AND PRODUCTION CONSENT
        """,
        null,
        false,
        1
    );

    var consentsToSection = documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        In accordance with the clause titled “Development and production programmes” set out in or otherwise \
        incorporated into the Licence(s), the Oil and Gas Authority hereby consents to:
        """,
        null,
        true,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        consentsToSection,
        "The erection or carrying out of the relevant works",
        """
        the erection or carrying out of the relevant works, as defined in the Licence(s) and described in the document \
        entitled ((FIELD_DEVELOPMENT_PLAN_TITLE)) dated ((FIELD_DEVELOPMENT_PLAN_DATE)) (the “Development Plan”), \
        during the Period (as defined in paragraph 2 below) for the purpose of getting petroleum from those parts \
        of the licensed area known as the ((PRIMARY_FIELD_NAME)) field, as defined in the Development Plan \
        (the “Field"), or for the purpose of conveying, to a place on land petroleum got from the Field; and
        """,
        null,
        true,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        consentsToSection,
        "The getting of petroleum",
        "the getting of petroleum from the Field during the Period as by means of such relevant works.",
        null,
        true,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        This consent shall commence on ((CONSENT_START_DATE)) and expire on the earlier of ((CONSENT_END_DATE)) or the \
        date of expiry or determination of any Licence (the “Period").
        """,
        null,
        true,
        3
    );

    var thisConsentIsGivenSection = documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent is given",
        "This consent is given subject always to the following conditions:",
        null,
        true,
        4
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        thisConsentIsGivenSection,
        "Any activities carried out pursuant",
        """
        any activities carried out pursuant to this consent shall be carried out in accordance with the Development \
        Plan; and
        """,
        null,
        true,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        thisConsentIsGivenSection,
        "During the Period, the quantity of petroleum",
        """
        during the Period, the quantity of petroleum got from the Field shall not be greater than the maximum quantity \
        nor less than the minimum quantity specified in the schedule hereto.
        """,
        null,
        true,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent is given for the purposes",
        """
        This consent is given for the purposes of the said clause titled “Development and production programmes” and \
        without prejudice to the operation of any other provision of the Licence(s) or otherwise.
        """,
        null,
        true,
        5
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the Oil and Gas Authority dated [DATE].]",
        null,
        true,
        6
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Consents to the use of gas",
        """
        In accordance with sub-clause (3)(b) of the clause titled “Avoidance of harmful methods of working” set out in \
        or otherwise incorporated into the Licence(s), the Oil and Gas Authority hereby consents to the use of gas for \
        the purpose of creating or increasing the pressure by means of which petroleum is obtained from the licensed \
        area of the Licence(s).
        """,
        "GAS_WILL_BE_INJECTED",
        true,
        7
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        8
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Backplate",
        """
        ((PRIMARY_FIELD_NAME))
        LICENCE(S) ((LICENCE_REFERENCE_LIST))
        [TODO FCS-611: All Licensees]
        """,
        null,
        false,
        9
    );
  }

  void createFieldFlareConsentDocumentTemplate() {
    var fieldFlareConsentDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.FIELD_FLARE_CONSENT.getMnemonic(),
        "Flare Consent",
        "Document template used for creating Flare Consents",
        "fcs/document/template/consent/flare/fieldFlareConsent.ftl",
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Header",
        """
        ((CONSENT_LENGTH_UPPER_CASE)) FLARE CONSENT
        PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)
        """,
        null,
        false,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        In accordance with paragraph (3)(a) of the clause titled “Avoidance of harmful methods of working” set out in \
        or otherwise incorporated into the Licence(s), the Oil and Gas Authority hereby consents to the flaring, of \
        natural gas obtained from those parts of the licensed area known as the ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES)) \
        field(s) during the Period.
        """,
        null,
        true,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        This consent is given subject always to the condition that natural gas shall not be flared at an average daily \
        rate greater than the maximum specified in the schedule hereto.
        """,
        null,
        true,
        3
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        This consent shall commence on ((CONSENT_START_DATE)) and expire on the earlier of ((CONSENT_END_DATE)) or the \
        date of expiry or determination of any Licence (the “Period").
        """,
        null,
        true,
        4
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent is given for the purposes",
        """
        This consent is given for the purposes of the said paragraph (3)(a) of the clause titled “Avoidance of harmful \
        methods of working” and without prejudice to the operation of any other provision of the Licence(s) or otherwise.
        """,
        null,
        true,
        5
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the Oil and Gas Authority dated [DATE].]",
        null,
        true,
        6
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        7
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Backplate",
        """
        ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES))
        LICENCE(S) ((LICENCE_REFERENCE_LIST))
        [TODO FCS-611: All Licensees]
        """,
        null,
        false,
        8
    );
  }

  void createFieldVentConsentDocumentTemplate() {
    documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.FIELD_VENT_CONSENT.getMnemonic(),
        "Vent Consent",
        "Document template used for creating Vent Consents",
        "fcs/document/template/consent/vent/fieldVentConsent.ftl",
        3
    );
  }
}
