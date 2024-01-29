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
import org.mockito.Spy;
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
  @Spy
  private DocumentTemplateBootstrapService documentTemplateBootstrapService;

  @Test
  void onApplicationReadyEvent_existingDocumentTemplateDtos() {
    when(documentTemplateService.getDocumentTemplateDtos())
        .thenReturn(List.of(DocumentTemplateDtoTestUtil.builder().build()));

    documentTemplateBootstrapService.onApplicationReadyEvent();

    verify(documentTemplateBootstrapService, never()).createFieldProductionConsentDocumentTemplate();
    verify(documentTemplateBootstrapService, never()).createFieldFlareConsentDocumentTemplate();
    verify(documentTemplateBootstrapService, never()).createFieldVentConsentDocumentTemplate();
  }

  @Test
  void onApplicationReadyEvent_noExistingDocumentTemplateDtos() {
    when(documentTemplateService.getDocumentTemplateDtos()).thenReturn(List.of());

    documentTemplateBootstrapService.onApplicationReadyEvent();

    verify(documentTemplateBootstrapService).createFieldProductionConsentDocumentTemplate();
    verify(documentTemplateBootstrapService).createFieldFlareConsentDocumentTemplate();
    verify(documentTemplateBootstrapService).createFieldVentConsentDocumentTemplate();
  }

  @Test
  void createFieldProductionConsentDocumentTemplate() {
    var fieldProductionConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    var consentsToSection = DocumentTemplateSectionDtoTestUtil.builder().build();
    var thisConsentIsGivenSection = DocumentTemplateSectionDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic(),
            "Production Consent",
            "Document template used for creating Production Consents",
            "fcs/document/template/consent/production/fieldProductionConsent.ftl",
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
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
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
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        consentsToSection,
        "The getting of petroleum",
        "the getting of petroleum from the Field during the Period as by means of such relevant works.",
        null,
        true,
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
        3
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "This consent is given",
        "This consent is given subject always to the following conditions:",
        null,
        true,
        4
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
        5
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the Oil and Gas Authority dated [DATE].]",
        null,
        true,
        6
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
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

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldProductionConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        8
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
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

  @Test
  void createFieldFlareConsentDocumentTemplate() {
    var fieldFlareConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.FIELD_FLARE_CONSENT.getMnemonic(),
            "Flare Consent",
            "Document template used for creating Flare Consents",
            "fcs/document/template/consent/flare/fieldFlareConsent.ftl",
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
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
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
        5
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "TODO FCS-610: This consent supersedes",
        "[This consent supersedes the consent [CONSENT REFERENCE] granted by the Oil and Gas Authority dated [DATE].]",
        null,
        true,
        6
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldFlareConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        7
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
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

  @Test
  void createFieldVentConsentDocumentTemplate() {
    var fieldVentConsentDocumentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    when(
        documentTemplateService.createDocumentTemplate(
            DocumentTemplateType.FIELD_VENT_CONSENT.getMnemonic(),
            "Vent Consent",
            "Document template used for creating Vent Consents",
            "fcs/document/template/consent/vent/fieldVentConsent.ftl",
            3
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
        1
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Consents to",
        """
        Pursuant to section 12A(1)(b) of the Energy Act 1976, the Oil and Gas Authority hereby consents to the \
        releasing unignited into the atmosphere of natural gas obtained from those parts of the licensed area of the \
        Licence(s) known as the ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES)) field(s) during the Period.
        """,
        null,
        true,
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
        4
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Schedule",
        "((SCHEDULE))",
        null,
        false,
        5
    );

    verify(documentTemplateSectionService).createDocumentTemplateSection(
        fieldVentConsentDocumentTemplateDto,
        null,
        "Backplate",
        """
        ((PRIMARY_AND_ADDITIONAL_FIELD_NAMES))
        LICENCE(S) ((LICENCE_REFERENCE_LIST))
        [TODO FCS-611: All Licensees]
        """,
        null,
        false,
        6
    );
  }
}
