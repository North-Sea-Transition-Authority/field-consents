package uk.co.nstauthority.fieldconsents.document.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;

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
    createTerminalFlareConsentDocumentTemplate();
    createFieldVentConsentDocumentTemplate();
    createTerminalVentConsentDocumentTemplate();
    createFlareAndCommissioningLetterDocumentTemplate();
  }

  void createFieldProductionConsentDocumentTemplate() {
    var fieldProductionConsentDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic(),
        "Field Production Consent",
        "Document template used for creating Field Production Consent documents",
        "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Header",
        """
        <p>Date: ((ISSUE_DATE))</p>\
        <p>\
        <strong>\
        PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)<br/>\
        ((CONSENT_LENGTH_UPPER_CASE))  DEVELOPMENT AND PRODUCTION CONSENT\
        </strong>\
        <p>\
        <p>To: Licensees of Petroleum Production Licences ((LICENCE_REFERENCE_LIST)) (listed in schedule 2 hereto).</p>\
        """,
        null,
        false,
        false,
        1
    );

    var consentsToSection = documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        <p style="text-align: justify;">\
        In accordance with the clause titled “Development and production programmes” set out in or otherwise \
        incorporated into the Licence(s), the ((REGULATOR_LEGAL_NAME)) hereby consents to:\
        </p>\
        """,
        null,
        true,
        false,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        consentsToSection,
        "The erection or carrying out of the relevant works",
        """
        <p style="text-align: justify;">\
        the erection or carrying out of the relevant works, as defined in the Licence(s) and described in the document \
        entitled ((FIELD_DEVELOPMENT_PLAN_TITLE)) dated ((FIELD_DEVELOPMENT_PLAN_DATE)) \
        (the “<strong>Development Plan</strong>”), during the Period (as defined in paragraph 2 below) for the purpose \
        of getting petroleum from those parts of the licensed area known as the \
        <strong>((PRIMARY_FIELD_NAME))</strong> field, as defined in the Development Plan \
        (the “<strong>Field</strong>”), or for the purpose of conveying, to a place on land petroleum got from the \
        Field; and \
        </p>\
        """,
        null,
        true,
        false,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        consentsToSection,
        "The getting of petroleum",
        """
        <p style="text-align: justify;">\
        the getting of petroleum from the Field during the Period as by means of such relevant works.\
        </p>\
        """,
        null,
        true,
        false,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        <p style="text-align: justify;">\
        This consent shall commence on ((CONSENT_START_DATE)) and expire on the earlier of ((CONSENT_END_DATE)) or the \
        date of expiry or determination of any Licence (the “<strong>Period</strong>”).\
        </p>\
        """,
        null,
        true,
        false,
        3
    );

    var thisConsentIsGivenSection = documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        <p style="text-align: justify;">\
        This consent is given subject always to the following conditions:\
        </p>\
        """,
        null,
        true,
        false,
        4
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        thisConsentIsGivenSection,
        "Any activities carried out pursuant",
        """
        <p style="text-align: justify;">\
        any activities carried out pursuant to this consent shall be carried out in accordance with the Development \
        Plan; and\
        </p>\
        """,
        null,
        true,
        false,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        thisConsentIsGivenSection,
        "During the Period, the quantity of petroleum",
        """
        <p style="text-align: justify;">\
        during the Period, the quantity of petroleum got from the Field shall not be greater than the maximum quantity \
        nor less than the minimum quantity specified in the schedule hereto.\
        </p>\
        """,
        null,
        true,
        false,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent is given for the purposes",
        """
        <p style="text-align: justify;">\
        This consent is given for the purposes of the said clause titled “Development and production programmes” and \
        without prejudice to the operation of any other provision of the Licence(s) or otherwise.\
        </p>\
        """,
        null,
        true,
        false,
        5
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent supersedes",
        """
        <p style="text-align: justify;">\
        This consent supersedes the consent ((SUPERSEDED_CONSENT_REFERENCE)) granted by the ((REGULATOR_LEGAL_NAME)) dated \
        ((SUPERSEDED_CONSENT_ISSUE_DATE)).\
        </p>\
        """,
        "APPLICATION_IS_REVISION",
        true,
        false,
        6
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Consents to the use of gas",
        """
        <p style="text-align: justify;">\
        In accordance with sub-clause (3)(b) of the clause titled “Avoidance of harmful methods of working” set out in \
        or otherwise incorporated into the Licence(s), the ((REGULATOR_LEGAL_NAME)) hereby consents to the use of gas \
        for the purpose of creating or increasing the pressure by means of which petroleum is obtained from the \
        licensed area of the Licence(s).\
        </p>\
        """,
        "GAS_WILL_BE_INJECTED",
        true,
        false,
        7
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Schedule 1",
        "((SCHEDULE))",
        null,
        false,
        true,
        8
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Schedule 2",
        """
        <p style="text-align: center;"><strong>SCHEDULE 2</strong></p>\
        <p><strong>Field</strong></p>\
        <p>((PRIMARY_FIELD_NAME))</p>\
        <p><strong>Licences</strong></p>\
        <p>((LICENCE_REFERENCE_LIST))</p>\
        <p><strong>Field Equity Partners</strong></p>\
        <p>((FIELD_EQUITY_PARTNER_LIST))</p>\
        """,
        null,
        false,
        true,
        9
    );
  }

  void createFieldFlareConsentDocumentTemplate() {
    var fieldFlareConsentDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.FIELD_FLARE_CONSENT.getMnemonic(),
        "Field Flare Consent",
        "Document template used for creating Field Flare Consent documents",
        "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Header",
        """
        <p>Date: ((ISSUE_DATE))</p>\
        <p>\
        <strong>\
        ((CONSENT_LENGTH_UPPER_CASE)) FLARE CONSENT<br/>\
        PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)<br/>\
        </strong>\
        </p>\
        <p>To: Licensees of Petroleum Production Licences ((LICENCE_REFERENCE_LIST)) (listed in schedule 2 hereto).</p>\
        """,
        null,
        false,
        false,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        <p style="text-align: justify;">\
        In accordance with paragraph (3)(a) of the clause titled “Avoidance of harmful methods of working” set out in \
        or otherwise incorporated into the Licence(s), the ((REGULATOR_LEGAL_NAME)) hereby consents to the flaring, of \
        natural gas obtained from those parts of the licensed area known as the ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES)) \
        field(s) during the Period.\
        </p>\
        """,
        null,
        true,
        false,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        <p style="text-align: justify;">\
        This consent is given subject always to the condition that natural gas shall not be flared at an average daily \
        rate greater than the maximum specified in the schedule hereto.\
        </p>\
        """,
        null,
        true,
        false,
        3
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        <p style="text-align: justify;">\
        This consent shall commence on ((CONSENT_START_DATE)) and expire on the earlier of ((CONSENT_END_DATE)) or the \
        date of expiry or determination of any Licence (the “<strong>Period</strong>”).\
        </p>\
        """,
        null,
        true,
        false,
        4
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent is given for the purposes",
        """
        <p style="text-align: justify;">\
        This consent is given for the purposes of the said paragraph (3)(a) of the clause titled “Avoidance of harmful \
        methods of working” and without prejudice to the operation of any other provision of the Licence(s) or \
        otherwise.\
        </p>\
        """,
        null,
        true,
        false,
        5
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent supersedes",
        """
        <p style="text-align: justify;">\
        This consent supersedes the consent ((SUPERSEDED_CONSENT_REFERENCE)) granted by the ((REGULATOR_LEGAL_NAME)) dated \
        ((SUPERSEDED_CONSENT_ISSUE_DATE)).\
        </p>\
        """,
        "APPLICATION_IS_REVISION",
        true,
        false,
        6
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Schedule 1",
        "((SCHEDULE))",
        null,
        false,
        true,
        7
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Schedule 2",
        """
        <p style="text-align: center;"><strong>SCHEDULE 2</strong></p>\
        <p><strong>Fields</strong></p>\
        <p>((PRIMARY_AND_ADDITIONAL_FIELD_NAMES))</p>\
        <p><strong>Licences</strong></p>\
        <p>((LICENCE_REFERENCE_LIST))</p>\
        <p><strong>Field Equity Partners</strong></p>\
        <p>((FIELD_EQUITY_PARTNER_LIST))</p>\
        """,
        null,
        false,
        true,
        8
    );
  }

  void createTerminalFlareConsentDocumentTemplate() {
    var terminalFlareConsentDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.TERMINAL_FLARE_CONSENT.getMnemonic(),
        "Facility Flare Consent",
        "Document template used for creating Facility Flare Consent documents",
        "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
        3
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "Header",
        """
        <p>Date: ((ISSUE_DATE))</p>\
        <p>\
        <strong>\
        ENERGY ACT 1976<br/>\
        CONSENT TO DISPOSE OF NATURAL GAS AT ((FACILITY_NAME))\
        </strong>\
        </p>\
        """,
        null,
        false,
        false,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        <p style="text-align: justify;">\
        Pursuant to section 12A(1)(a) of the Energy Act 1976, the ((REGULATOR_LEGAL_NAME)) hereby consents to the \
        flaring from the relevant oil processing facility or relevant gas processing facility, being the \
        ((FACILITY_NAME)) (the “<strong>Facility</strong>”), of natural gas originally won from the fields with a \
        right to have the natural gas processed by the Facility.\
        </p>\
        """,
        null,
        true,
        false,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        <p style="text-align: justify;">\
        This consent shall commence on ((CONSENT_START_DATE)) and expire on ((CONSENT_END_DATE)) \
        (the “<strong>Period</strong>”).\
        </p>\
        """,
        null,
        true,
        false,
        3
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        <p style="text-align: justify;">\
        This consent is given subject always to the condition that, during the Period, natural gas shall not be flared \
        at an average daily rate greater than the maximum specified in the schedule hereto.\
        </p>\
        """,
        null,
        true,
        false,
        4
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "This consent supersedes",
        """
        <p style="text-align: justify;">\
        This consent supersedes the consent ((SUPERSEDED_CONSENT_REFERENCE)) granted by the ((REGULATOR_LEGAL_NAME)) dated \
        ((SUPERSEDED_CONSENT_ISSUE_DATE)).\
        </p>\
        """,
        "APPLICATION_IS_REVISION",
        true,
        false,
        5
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        true,
        6
    );
  }

  void createFieldVentConsentDocumentTemplate() {
    var fieldVentConsentDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.FIELD_VENT_CONSENT.getMnemonic(),
        "Field Vent Consent",
        "Document template used for creating Field Vent Consent documents",
        "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
        4
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Header",
        """
        <p>Date: ((ISSUE_DATE))</p>\
        <p>\
        <strong>\
        ENERGY ACT 1976<br/>\
        PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)<br/>\
        ((CONSENT_LENGTH_UPPER_CASE)) VENT CONSENT\
        </strong>\
        </p>\
        <p>To: Licensees of Petroleum Production Licences ((LICENCE_REFERENCE_LIST)) (listed in schedule 2 hereto).</p>\
        """,
        null,
        false,
        false,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        <p style="text-align: justify;">\
        Pursuant to section 12A(1)(b) of the Energy Act 1976, the ((REGULATOR_LEGAL_NAME)) hereby consents to the \
        releasing unignited into the atmosphere of natural gas obtained from those parts of the licensed area of the \
        Licence(s) known as the ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES)) field(s) during the Period.\
        </p>\
        """,
        null,
        true,
        false,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        <p style="text-align: justify;">\
        This consent shall commence on ((CONSENT_START_DATE)) and expire on the earlier of ((CONSENT_END_DATE)) or the \
        date of expiry or determination of any Licence (the “<strong>Period</strong>”).\
        </p>\
        """,
        null,
        true,
        false,
        3
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        <p style="text-align: justify;">\
        This consent is given subject always to the condition that natural gas shall not be disposed of at an average \
        daily rate greater than the maximum specified in the schedule hereto.\
        </p>\
        """,
        null,
        true,
        false,
        4
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "This consent supersedes",
        """
        <p style="text-align: justify;">\
        This consent supersedes the consent ((SUPERSEDED_CONSENT_REFERENCE)) granted by the ((REGULATOR_LEGAL_NAME)) dated \
        ((SUPERSEDED_CONSENT_ISSUE_DATE)).\
        </p>\
        """,
        "APPLICATION_IS_REVISION",
        true,
        false,
        5
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Schedule 1",
        "((SCHEDULE))",
        null,
        false,
        true,
        6
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Schedule 2",
        """
        <p style="text-align: center;"><strong>SCHEDULE 2</strong></p>\
        <p><strong>Fields</strong></p>\
        <p>((PRIMARY_AND_ADDITIONAL_FIELD_NAMES))</p>\
        <p><strong>Licences</strong></p>\
        <p>((LICENCE_REFERENCE_LIST))</p>\
        <p><strong>Field Equity Partners</strong></p>\
        <p>((FIELD_EQUITY_PARTNER_LIST))</p>\
        """,
        null,
        false,
        true,
        7
    );
  }

  void createTerminalVentConsentDocumentTemplate() {
    var terminalVentConsentDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.TERMINAL_VENT_CONSENT.getMnemonic(),
        "Facility Vent Consent",
        "Document template used for creating Facility Vent Consent documents",
        "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
        5
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "Header",
        """
        <p>Date: ((ISSUE_DATE))</p>
        <p>\
        <strong>\
        ENERGY ACT 1976<br/>\
        CONSENT TO DISPOSE OF NATURAL GAS AT ((FACILITY_NAME))\
        </strong>\
        </p>\
        """,
        null,
        false,
        false,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        <p style="text-align: justify;">\
        Pursuant to section 12A(1)(a) of the Energy Act 1976, the ((REGULATOR_LEGAL_NAME)) hereby consents to the \
        releasing unignited into the atmosphere from the relevant oil processing facility or relevant gas processing \
        facility, being the ((FACILITY_NAME)) (the “<strong>Facility</strong>”), of natural gas originally won from \
        the fields with a right to have the natural gas processed by the Facility.\
        </p>\
        """,
        null,
        true,
        false,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        <p style="text-align: justify;">\
        This consent shall commence on ((CONSENT_START_DATE)) and expire on ((CONSENT_END_DATE)) \
        (the “<strong>Period</strong>”).\
        </p>\
        """,
        null,
        true,
        false,
        3
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        <p style="text-align: justify;">\
        This consent is given subject always to the condition that, during the Period, natural gas shall not be \
        disposed of at an average daily rate greater than the maximum specified in the schedule hereto.\
        </p>\
        """,
        null,
        true,
        false,
        4
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "This consent supersedes",
        """
        <p style="text-align: justify;">\
        This consent supersedes the consent ((SUPERSEDED_CONSENT_REFERENCE)) granted by the ((REGULATOR_LEGAL_NAME)) dated \
        ((SUPERSEDED_CONSENT_ISSUE_DATE)).\
        </p>\
        """,
        "APPLICATION_IS_REVISION",
        true,
        false,
        5
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        true,
        6
    );
  }

  void createFlareAndCommissioningLetterDocumentTemplate() {
    var flaringAndCommissioningLetterDocumentTemplateDto = documentTemplateService.createDocumentTemplate(
        DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER.getMnemonic(),
        "Flare and Commissioning Letter",
        "Document template used for creating Flare and Commissioning Letter documents",
        "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
        6
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        flaringAndCommissioningLetterDocumentTemplateDto,
        null,
        "Cover letter",
        """
        <p style="text-align: right">\
        Regulation Directorate<br/>\
        3rd Floor<br/>\
        1 Marischal Square<br/>\
        Broad Street<br/>\
        Aberdeen<br/>\
        AB10 1BL<br/>\
        </p>\
        <p>((ISSUE_DATE))</p>\
        <p>\
        <strong>\
        <u>\
        ((PRIMARY_FIELD_NAME)) Field – Serving Notice for Flaring and Commissioning Strategy Requested\
        </u>\
        </strong>\
        </p>\
        <p style="text-align: justify;">\
        I now serve the formal notice in Annex A which invites you to make an application in writing for consent to \
        flare gas throughout the life of field. Annex B outlines the requirements for the Commissioning Strategy \
        document which must be submitted 6 months prior to first hydrocarbons for ((REGULATOR_MNEMONIC)) \
        consideration.\
        </p>\
        <p style="text-align: justify;">\
        The ((REGULATOR_NAME)) (((REGULATOR_MNEMONIC))) is the business name of the ((REGULATOR_LEGAL_NAME)) \
        (((REGULATOR_LEGAL_MNEMONIC))). The ((REGULATOR_LEGAL_MNEMONIC)) remains the legal name of the company. \
        References to the ((REGULATOR_MNEMONIC)) should be interpreted as the ((REGULATOR_LEGAL_MNEMONIC)).\
        </p>\
        <p>\
        Yours sincerely<br/>\
        ((CONSENTS_TEAM_NAME))\
        </p>\
        """,
        null,
        false,
        false,
        1
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        flaringAndCommissioningLetterDocumentTemplateDto,
        null,
        "Annex A",
        """
        <p><strong>Annex A</strong></p>\
        <p>\
        <strong>\
        PETROLEUM PRODUCTION LICENCE NO(S) ((LICENCE_REFERENCE_LIST))<br/>\
        APPLICATIONS TO FLARE GAS FROM THE ((PRIMARY_FIELD_NAME)) FIELD\
        </strong>\
        </p>\
        <p style="text-align: justify;">\
        The ((REGULATOR_LEGAL_NAME)) hereby gives notice, in accordance with paragraph (3)(a) of the clause titled \
        “Avoidance of harmful methods of working” set out in or otherwise incorporated into the Licence(s) that, in \
        consequence of plans comprised in the document entitled “((FIELD_DEVELOPMENT_PLAN_TITLE))” submitted to the \
        ((REGULATOR_LEGAL_NAME)) on ((FIELD_DEVELOPMENT_PLAN_DATE)) for the getting of petroleum from those parts of \
        the licensed area known as the ((PRIMARY_FIELD_NAME)) field, which the ((REGULATOR_LEGAL_NAME)) considers \
        reasonable, the ((REGULATOR_LEGAL_NAME)) will entertain applications in writing from ((PRIMARY_OPERATOR_NAME)) \
        for consent to flare gas throughout the life of the ((PRIMARY_FIELD_NAME)) field in any case where the \
        application specifies the date on which it is proposed flaring should commence, being a date not before the \
        expiration of 7 days, beginning with the date on which the ((REGULATOR_LEGAL_NAME)) receives the application.\
        </p>\
        <p style="text-align: justify;">\
        The ((REGULATOR_NAME)) (((REGULATOR_MNEMONIC))) is the business name of the ((REGULATOR_LEGAL_NAME)) \
        (((REGULATOR_LEGAL_MNEMONIC))). The ((REGULATOR_LEGAL_MNEMONIC)) remains the legal name of the company. \
        References to the ((REGULATOR_MNEMONIC)) should be interpreted as the ((REGULATOR_LEGAL_MNEMONIC)).\
        </p>\
        <p>\
        Yours sincerely<br/>\
        ((CONSENTS_TEAM_NAME))\
        </p>\
        """,
        null,
        false,
        true,
        2
    );

    documentTemplateSectionService.createDocumentTemplateSection(
        flaringAndCommissioningLetterDocumentTemplateDto,
        null,
        "Annex B",
        """
        <p><strong>Annex B</strong></p>\
        <p><strong>((PRIMARY_FIELD_NAME)) FIELD – START-UP FLARE CONSENT AND COMMISSIONING STRATEGY</strong></p>\
        <p style="text-align: justify;">\
        I now outline the start-up requirements for the ((PRIMARY_FIELD_NAME)) Field. The document should include the \
        following: -\
        </p>\
        <p style="text-align: justify;">\
        <strong>History of ((PRIMARY_FIELD_NAME))</strong> – An introductory paragraph covering the history of the \
        Field.\
        </p>\
        <p style="text-align: justify;">\
        <strong>Production Facilities</strong> – A brief description of the production facilities and how the various \
        components will be commissioned from start-up to the end of the commissioning period.\
        </p>\
        <p style="text-align: justify;">\
        <strong>Commissioning</strong> – Details of how the system(s) will be commissioned, how wells will be brought \
        on stream during the commissioning period, the anticipated production rates, the commissioning milestones on \
        the gas side (where applicable) e.g. predicted date of fuel gas, gas export, the length of time to reach \
        stability and the design flaring level.\
        </p>\
        <p style="text-align: justify;">\
        <strong>Flaring / Venting</strong> - An outline of the anticipated flaring during the commissioning period and \
        target flare for the field when it is operating at stable conditions. Anticipated flaring levels for the first \
        28 days of production, with assumptions. Consents to flare gas will be normally remain on short-term periods \
        until there is an improvement in the plant and stability is reached.\
        </p>\
        <p style="text-align: justify;">\
        <strong>Reporting</strong> – During the period of short-term consents, reports will normally be called for on \
        a weekly basis. These will normally include:\
        </p>\
        <p>a) Details of the gas handling plant during the period.</p>\
        <p>b) Daily rates in respect of oil and gas production, fuel gas, gas export and gas flare rates.</p>\
        <p>c) Cumulative averages for production and flare.</p>\
        <p>d) Monthly calculations of gas compressor efficiency.</p>\
        <p>\
        Once we have received and reviewed your commissioning strategy document, we may be in touch to arrange a \
        meeting to discuss the plan in more detail. Should you have any questions regarding the content please do not \
        hesitate to contact us.\
        </p>\
        <p>Please email your plan to ((REGULATOR_EMAIL)).</p>\
        <p style="text-align: justify;">\
        The ((REGULATOR_NAME)) (((REGULATOR_MNEMONIC))) is the business name of the ((REGULATOR_LEGAL_NAME)) \
        (((REGULATOR_LEGAL_MNEMONIC))). The ((REGULATOR_LEGAL_MNEMONIC)) remains the legal name of the company. \
        References to the ((REGULATOR_MNEMONIC)) should be interpreted as the ((REGULATOR_LEGAL_MNEMONIC)).\
        </p>\
        <p>\
        Yours sincerely<br/>\
        ((CONSENTS_TEAM_NAME))\
        </p>\
        """,
        null,
        false,
        true,
        3
    );
  }
}
