package uk.co.nstauthority.fieldconsents.document.template;

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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateSectionService;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateService;

@ExtendWith(MockitoExtension.class)
class DocumentTemplateBootstrapServiceTest {

  @Mock
  private DocumentTemplateService documentTemplateService;

  @Mock
  private DocumentTemplateSectionService documentTemplateSectionService;

  @InjectMocks
  @Spy
  private DocumentTemplateBootstrapService documentTemplateBootstrapService;

  @Test
  void onApplicationReadyEvent_existingDocumentTemplateDtos() {
    when(documentTemplateService.getDocumentTemplateDtos())
        .thenReturn(List.of(DocumentTemplateDtoTestUtil.builder().build()));

    documentTemplateBootstrapService.onApplicationReadyEvent();

    verify(documentTemplateBootstrapService, never()).createFieldProductionConsentDocumentTemplate();
    verify(documentTemplateBootstrapService, never()).createFieldFlareConsentDocumentTemplate();
    verify(documentTemplateBootstrapService, never()).createTerminalFlareConsentDocumentTemplate();
    verify(documentTemplateBootstrapService, never()).createFieldVentConsentDocumentTemplate();
    verify(documentTemplateBootstrapService, never()).createTerminalVentConsentDocumentTemplate();
    verify(documentTemplateBootstrapService, never()).createFlareAndCommissioningLetterDocumentTemplate();
  }

  @Test
  void onApplicationReadyEvent_noExistingDocumentTemplateDtos() {
    when(documentTemplateService.getDocumentTemplateDtos()).thenReturn(List.of());

    documentTemplateBootstrapService.onApplicationReadyEvent();

    verify(documentTemplateBootstrapService).createFieldProductionConsentDocumentTemplate();
    verify(documentTemplateBootstrapService).createFieldFlareConsentDocumentTemplate();
    verify(documentTemplateBootstrapService).createTerminalFlareConsentDocumentTemplate();
    verify(documentTemplateBootstrapService).createFieldVentConsentDocumentTemplate();
    verify(documentTemplateBootstrapService).createTerminalVentConsentDocumentTemplate();
    verify(documentTemplateBootstrapService).createFlareAndCommissioningLetterDocumentTemplate();
  }

  @Test
  void createFieldProductionConsentDocumentTemplate() {
    var fieldProductionConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var consentsToSection = DocumentTemplateSectionDtoTestUtil.builder().build();
    var thisConsentIsGivenSection = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic(),
            "Field Production Consent",
            "Document template used for creating Field Production Consent documents",
            "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
            1
        )
    ).thenReturn(fieldProductionConsentDocumentTemplateDto);

    when(
        documentTemplateSectionService.createDocumentTemplateSection(
            any(),
            any(),
            any(),
            any(),
            any(),
            anyBoolean(),
            anyBoolean(),
            anyInt()
        )
    ).thenReturn(null, consentsToSection, thisConsentIsGivenSection);

    documentTemplateBootstrapService.createFieldProductionConsentDocumentTemplate();

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Header",
        """
        PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)
        ((CONSENT_LENGTH_UPPER_CASE)) DEVELOPMENT AND PRODUCTION CONSENT
        """,
        null,
        false,
        false,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
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
        false,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        consentsToSection,
        "The getting of petroleum",
        "the getting of petroleum from the Field during the Period as by means of such relevant works.",
        null,
        true,
        false,
        2
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        This consent shall commence on ((CONSENT_START_DATE)) and expire on the earlier of ((CONSENT_END_DATE)) or the \
        date of expiry or determination of any Licence (the “Period").
        """,
        null,
        true,
        false,
        3
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        thisConsentIsGivenSection,
        "Any activities carried out pursuant",
        """
        any activities carried out pursuant to this consent shall be carried out in accordance with the Development \
        Plan; and
        """,
        null,
        true,
        false,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        thisConsentIsGivenSection,
        "During the Period, the quantity of petroleum",
        """
        during the Period, the quantity of petroleum got from the Field shall not be greater than the maximum quantity \
        nor less than the minimum quantity specified in the schedule hereto.
        """,
        null,
        true,
        false,
        2
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent is given for the purposes",
        """
        This consent is given for the purposes of the said clause titled “Development and production programmes” and \
        without prejudice to the operation of any other provision of the Licence(s) or otherwise.
        """,
        null,
        true,
        false,
        5
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the ((REGULATOR_LEGAL_NAME)) dated [DATE].]",
        null,
        true,
        false,
        6
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Consents to the use of gas",
        """
        In accordance with sub-clause (3)(b) of the clause titled “Avoidance of harmful methods of working” set out in \
        or otherwise incorporated into the Licence(s), the ((REGULATOR_LEGAL_NAME)) hereby consents to the use of gas \
        for the purpose of creating or increasing the pressure by means of which petroleum is obtained from the \
        licensed area of the Licence(s).
        """,
        "GAS_WILL_BE_INJECTED",
        true,
        false,
        7
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        true,
        8
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Backplate",
        """
        ((PRIMARY_FIELD_NAME))
        LICENCE(S) ((LICENCE_REFERENCE_LIST))
        FIELD EQUITY PARTNER(S) ((FIELD_EQUITY_PARTNER_NAME_LIST))
        """,
        null,
        false,
        true,
        9
    );
  }

  @Test
  void createFieldFlareConsentDocumentTemplate() {
    var fieldFlareConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.FIELD_FLARE_CONSENT.getMnemonic(),
            "Field Flare Consent",
            "Document template used for creating Field Flare Consent documents",
            "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
            2
        )
    ).thenReturn(fieldFlareConsentDocumentTemplateDto);

    documentTemplateBootstrapService.createFieldFlareConsentDocumentTemplate();

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Header",
        """
        ((CONSENT_LENGTH_UPPER_CASE)) FLARE CONSENT
        PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)
        """,
        null,
        false,
        false,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        In accordance with paragraph (3)(a) of the clause titled “Avoidance of harmful methods of working” set out in \
        or otherwise incorporated into the Licence(s), the ((REGULATOR_LEGAL_NAME)) hereby consents to the flaring, of \
        natural gas obtained from those parts of the licensed area known as the ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES)) \
        field(s) during the Period.
        """,
        null,
        true,
        false,
        2
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        This consent is given subject always to the condition that natural gas shall not be flared at an average daily \
        rate greater than the maximum specified in the schedule hereto.
        """,
        null,
        true,
        false,
        3
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        This consent shall commence on ((CONSENT_START_DATE)) and expire on the earlier of ((CONSENT_END_DATE)) or the \
        date of expiry or determination of any Licence (the “Period").
        """,
        null,
        true,
        false,
        4
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "This consent is given for the purposes",
        """
        This consent is given for the purposes of the said paragraph (3)(a) of the clause titled “Avoidance of harmful \
        methods of working” and without prejudice to the operation of any other provision of the Licence(s) or otherwise.
        """,
        null,
        true,
        false,
        5
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the ((REGULATOR_LEGAL_NAME)) dated [DATE].]",
        null,
        true,
        false,
        6
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        true,
        7
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Backplate",
        """
        ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES))
        LICENCE(S) ((LICENCE_REFERENCE_LIST))
        FIELD EQUITY PARTNER(S) ((FIELD_EQUITY_PARTNER_NAME_LIST))
        """,
        null,
        false,
        true,
        8
    );
  }

  @Test
  void createTerminalFlareConsentDocumentTemplate() {
    var terminalFlareConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.TERMINAL_FLARE_CONSENT.getMnemonic(),
            "Facility Flare Consent",
            "Document template used for creating Facility Flare Consent documents",
            "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
            3
        )
    ).thenReturn(terminalFlareConsentDocumentTemplateDto);

    documentTemplateBootstrapService.createTerminalFlareConsentDocumentTemplate();

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "Header",
        """
        ENERGY ACT 1976
        CONSENT TO DISPOSE OF NATURAL GAS AT ((FACILITY_NAME))
        """,
        null,
        false,
        false,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        Pursuant to section 12A(1)(a) of the Energy Act 1976, the ((REGULATOR_LEGAL_NAME)) hereby consents to the \
        flaring from the relevant oil processing facility or relevant gas processing facility, being the \
        ((FACILITY_NAME)) (the “Facility”), of natural gas originally won from the fields with a right to have the \
        natural gas processed by the Facility.
        """,
        null,
        true,
        false,
        2
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        "This consent shall commence on ((CONSENT_START_DATE)) and expire on ((CONSENT_END_DATE)) (the “Period”).",
        null,
        true,
        false,
        3
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        This consent is given subject always to the condition that, during the Period, natural gas shall not be flared \
        at an average daily rate greater than the maximum specified in the schedule hereto.
        """,
        null,
        true,
        false,
        4
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalFlareConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the ((REGULATOR_LEGAL_NAME)) dated [DATE].]",
        null,
        true,
        false,
        5
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
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

  @Test
  void createFieldVentConsentDocumentTemplate() {
    var fieldVentConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.FIELD_VENT_CONSENT.getMnemonic(),
            "Field Vent Consent",
            "Document template used for creating Field Vent Consent documents",
            "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
            4
        )
    ).thenReturn(fieldVentConsentDocumentTemplateDto);

    documentTemplateBootstrapService.createFieldVentConsentDocumentTemplate();

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Header",
        """
        ENERGY ACT 1976
        PETROLEUM PRODUCTION LICENCE No(s). ((LICENCE_REFERENCE_LIST)) (“Licence(s)”)
        ((CONSENT_LENGTH_UPPER_CASE)) VENT CONSENT
        """,
        null,
        false,
        false,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        Pursuant to section 12A(1)(b) of the Energy Act 1976, the ((REGULATOR_LEGAL_NAME)) hereby consents to the \
        releasing unignited into the atmosphere of natural gas obtained from those parts of the licensed area of the \
        Licence(s) known as the ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES)) field(s) during the Period.
        """,
        null,
        true,
        false,
        2
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        """
        This consent shall commence on ((CONSENT_START_DATE)) and expire on the earlier of ((CONSENT_END_DATE)) or the \
        date of expiry or determination of any Licence (the “Period").
        """,
        null,
        true,
        false,
        3
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        This consent is given subject always to the condition that natural gas shall not be disposed of at an average \
        daily rate greater than the maximum specified in the schedule hereto.
        """,
        null,
        true,
        false,
        4
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the ((REGULATOR_LEGAL_NAME)) dated [DATE].]",
        null,
        true,
        false,
        5
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        true,
        6
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Backplate",
        """
        ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES))
        LICENCE(S) ((LICENCE_REFERENCE_LIST))
        FIELD EQUITY PARTNER(S) ((FIELD_EQUITY_PARTNER_NAME_LIST))
        """,
        null,
        false,
        true,
        7
    );
  }

  @Test
  void createTerminalVentConsentDocumentTemplate() {
    var terminalVentConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.TERMINAL_VENT_CONSENT.getMnemonic(),
            "Facility Vent Consent",
            "Document template used for creating Facility Vent Consent documents",
            "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
            5
        )
    ).thenReturn(terminalVentConsentDocumentTemplateDto);

    documentTemplateBootstrapService.createTerminalVentConsentDocumentTemplate();

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "Header",
        """
        ENERGY ACT 1976
        CONSENT TO DISPOSE OF NATURAL GAS AT ((FACILITY_NAME))
        """,
        null,
        false,
        false,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        Pursuant to section 12A(1)(a) of the Energy Act 1976, the ((REGULATOR_LEGAL_NAME)) hereby consents to the \
        releasing unignited into the atmosphere from the relevant oil processing facility or relevant gas processing \
        facility, being the ((FACILITY_NAME)) (the “Facility”), of natural gas originally won from the fields with a \
        right to have the natural gas processed by the Facility.
        """,
        null,
        true,
        false,
        2
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "This consent shall commence on",
        "This consent shall commence on ((CONSENT_START_DATE)) and expire on ((CONSENT_END_DATE)) (the “Period”).",
        null,
        true,
        false,
        3
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "This consent is given",
        """
        This consent is given subject always to the condition that, during the Period, natural gas shall not be \
        disposed of at an average daily rate greater than the maximum specified in the schedule hereto.
        """,
        null,
        true,
        false,
        4
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        terminalVentConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the ((REGULATOR_LEGAL_NAME)) dated [DATE].]",
        null,
        true,
        false,
        5
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
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

  @Test
  void createFlareAndCommissioningLetterDocumentTemplate() {
    var flaringAndCommissioningLetterDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER.getMnemonic(),
            "Flare and Commissioning Letter",
            "Document template used for creating Flare and Commissioning Letter documents",
            "fcs/application/caseprocessing/document/instance/pdftemplate/document.ftl",
            6
        )
    ).thenReturn(flaringAndCommissioningLetterDocumentTemplateDto);

    documentTemplateBootstrapService.createFlareAndCommissioningLetterDocumentTemplate();

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        flaringAndCommissioningLetterDocumentTemplateDto,
        null,
        "Cover letter",
        """
        Regulation Directorate
        3rd Floor
        1 Marischal Square
        Broad Street
        Aberdeen
        AB10 1BL
        
        ((ISSUE_DATE))
                   
        ((PRIMARY_FIELD_NAME)) Field – Serving Notice for Flaring and Commissioning Strategy Requested
                
        I now serve the formal notice in Annex A which invites you to make an application in writing for consent to flare gas \
        throughout the life of field. Annex B outlines the requirements for the Commissioning Strategy document which must be \
        submitted 6 months prior to first hydrocarbons for ((REGULATOR_MNEMONIC)) consideration.
                
        The ((REGULATOR_NAME)) (((REGULATOR_MNEMONIC))) is the business name of the ((REGULATOR_LEGAL_NAME)) \
        (((REGULATOR_LEGAL_MNEMONIC))). The ((REGULATOR_LEGAL_MNEMONIC)) remains the legal name of the company. References to \
        the ((REGULATOR_MNEMONIC)) should be interpreted as the ((REGULATOR_LEGAL_MNEMONIC)).
                
        Yours sincerely
        ((CONSENTS_TEAM_NAME))
        """,
        null,
        false,
        false,
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        flaringAndCommissioningLetterDocumentTemplateDto,
        null,
        "Annex A",
        """
        Annex A

        PETROLEUM PRODUCTION LICENCE NO(S) ((LICENCE_REFERENCE_LIST))
        APPLICATIONS TO FLARE GAS FROM THE ((PRIMARY_FIELD_NAME)) FIELD

        The ((REGULATOR_LEGAL_NAME)) hereby gives notice, in accordance with paragraph (3)(a) of the clause titled “Avoidance of \
        harmful methods of working” set out in or otherwise incorporated into the Licence(s) that, in consequence of plans \
        comprised in the document entitled “((FIELD_DEVELOPMENT_PLAN_TITLE))” submitted to the ((REGULATOR_LEGAL_NAME)) on \
        ((FIELD_DEVELOPMENT_PLAN_DATE)) for the getting of petroleum from those parts of the licensed area known as the \
        ((PRIMARY_FIELD_NAME)) field, which the ((REGULATOR_LEGAL_NAME)) considers reasonable, the ((REGULATOR_LEGAL_NAME)) will \
        entertain applications in writing from ((PRIMARY_OPERATOR_NAME)) for consent to flare gas throughout the life of the \
        ((PRIMARY_FIELD_NAME)) field in any case where the application specifies the date on which it is proposed flaring should \
        commence, being a date not before the expiration of 7 days, beginning with the date on which the \
        ((REGULATOR_LEGAL_NAME)) receives the application.

        The ((REGULATOR_NAME)) (((REGULATOR_MNEMONIC))) is the business name of the ((REGULATOR_LEGAL_NAME)) \
        (((REGULATOR_LEGAL_MNEMONIC))). The ((REGULATOR_LEGAL_MNEMONIC)) remains the legal name of the company. References to \
        the ((REGULATOR_MNEMONIC)) should be interpreted as the ((REGULATOR_LEGAL_MNEMONIC)).

        Yours sincerely
        ((CONSENTS_TEAM_NAME))
        """,
        null,
        false,
        true,
        2
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        flaringAndCommissioningLetterDocumentTemplateDto,
        null,
        "Annex B",
        """
        Annex B

        ((PRIMARY_FIELD_NAME)) FIELD – START-UP FLARE CONSENT AND COMMISSIONING STRATEGY

        I now outline the start-up requirements for the ((PRIMARY_FIELD_NAME)) Field. The document should include the following: -

        History of ((PRIMARY_FIELD_NAME)) – An introductory paragraph covering the history of the Field.

        Production Facilities – A brief description of the production facilities and how the various components will be \
        commissioned from start-up to the end of the commissioning period.

        Commissioning – Details of how the system(s) will be commissioned, how wells will be brought on stream during the \
        commissioning period, the anticipated production rates, the commissioning milestones on the gas side (where applicable) \
        e.g. predicted date of fuel gas, gas export, the length of time to reach stability and the design flaring level.

        Flaring / Venting - An outline of the anticipated flaring during the commissioning period and target flare for the field \
        when it is operating at stable conditions. Anticipated flaring levels for the first 28 days of production, with \
        assumptions. Consents to flare gas will be normally remain on short-term periods until there is an improvement in the \
        plant and stability is reached.

        Reporting – During the period of short-term consents, reports will normally be called for on a weekly basis. These will \
        normally include:

        a) Details of the gas handling plant during the period.

        b) Daily rates in respect of oil and gas production, fuel gas, gas export and gas flare rates.

        c) Cumulative averages for production and flare.

        d) Monthly calculations of gas compressor efficiency.

        Once we have received and reviewed your commissioning strategy document, we may be in touch to arrange a meeting to \
        discuss the plan in more detail. Should you have any questions regarding the content please do not hesitate to contact us.

        Please email your plan to ((REGULATOR_EMAIL)).
        
        The ((REGULATOR_NAME)) (((REGULATOR_MNEMONIC))) is the business name of the ((REGULATOR_LEGAL_NAME)) \
        (((REGULATOR_LEGAL_MNEMONIC))). The ((REGULATOR_LEGAL_MNEMONIC)) remains the legal name of the company. References to \
        the ((REGULATOR_MNEMONIC)) should be interpreted as the ((REGULATOR_LEGAL_MNEMONIC)).

        Yours sincerely
        ((CONSENTS_TEAM_NAME))
        """,
        null,
        false,
        true,
        3
    );
  }
}
