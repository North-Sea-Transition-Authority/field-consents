package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.FreeMarkerTemplateRenderingService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionLongTermFiguresService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class ScheduleMailMergeFieldTest {

  @Mock
  private ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;

  @Mock
  private ConsentDataService consentDataService;

  @Mock
  private ConsentProductionLongTermFiguresService consentProductionLongTermFiguresService;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;

  @Mock
  private PrimaryFieldNameMailMergeField primaryFieldNameMailMergeField;

  @Mock
  private ConsentStartDateMailMergeField consentStartDateMailMergeField;

  @Mock
  private ConsentEndDateMailMergeField consentEndDateMailMergeField;

  @InjectMocks
  private ScheduleMailMergeField scheduleMailMergeField;

  @Test
  void getMnemonic() {
    assertThat(scheduleMailMergeField.getMnemonic()).isEqualTo("SCHEDULE");
  }

  @Test
  void getDescription() {
    assertThat(scheduleMailMergeField.getDescription()).isEqualTo("The schedule for the Consent");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(scheduleMailMergeField.isApplicable(documentTemplateDto)).isEqualTo(documentTemplateType.isConsent());
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.INCLUDE)
  void resolve_documentTemplateTypeIsFieldProductionConsentAndConsentLengthTypeIsShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(
            DocumentTemplateDtoTestUtil.builder()
                .withMnemonic(DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic())
                .build()
        )
        .build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var primaryFieldName = "Test primary field name";

    var consentStartDate = "17/01/2024";
    var consentEndDate = "17/01/2024";

    var html = "<html></html>";

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(consentDataService.getConsentData(applicationVersion.getApplication())).thenReturn(consentData);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(primaryFieldNameMailMergeField.resolve(documentInstanceDto)).thenReturn(DocumentMailMergeFieldResolveResult.success(primaryFieldName));
    when(consentStartDateMailMergeField.resolve(documentInstanceDto)).thenReturn(DocumentMailMergeFieldResolveResult.success(consentStartDate));
    when(consentEndDateMailMergeField.resolve(documentInstanceDto)).thenReturn(DocumentMailMergeFieldResolveResult.success(consentEndDate));

    when(
        freeMarkerTemplateRenderingService.renderTemplate(
            "fcs/application/caseprocessing/document/instance/pdftemplate/consent/production/shortTermOrAnnualProductionConsentSchedule.ftl",
            Map.of(
                "capitalizedConsentLengthType",
                WordUtils.capitalizeFully(consentLengthType.getShortDisplayName()),
                "primaryFieldName",
                primaryFieldName,
                "consentStartDate",
                consentStartDate,
                "consentEndDate",
                consentEndDate,
                "consentProductionFiguresView",
                ConsentProductionFiguresView.fromShortTermOrAnnualConsentProductionFigures(consentData)
            )
        )
    ).thenReturn(html);

    assertThat(scheduleMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(html));
  }

  @Test
  void resolve_documentTemplateTypeIsFieldProductionConsentAndConsentLengthTypeIsLongTerm() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(
            DocumentTemplateDtoTestUtil.builder()
                .withMnemonic(DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic())
                .build()
        )
        .build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var longTermProductionConsentProductionFromDate = LocalDate.parse("2024-02-23");

    var consentData = ConsentDataTestUtil.newBuilder()
        .withLongTermProductionConsentProductionFromDate(longTermProductionConsentProductionFromDate)
        .build();

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);

    var primaryFieldName = "Test primary field name";

    var consentEndDate = "17/01/2024";

    var consentProductionFiguresViews = Map.of(
        "2024", mock(ConsentProductionFiguresView.class),
        "2025", mock(ConsentProductionFiguresView.class)
    );

    var html = "<html></html>";

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(consentDataService.getConsentData(applicationVersion.getApplication())).thenReturn(consentData);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(primaryFieldNameMailMergeField.resolve(documentInstanceDto)).thenReturn(DocumentMailMergeFieldResolveResult.success(primaryFieldName));
    when(consentEndDateMailMergeField.resolve(documentInstanceDto)).thenReturn(DocumentMailMergeFieldResolveResult.success(consentEndDate));
    when(consentProductionLongTermFiguresService.getConsentProductionLongTermFiguresViews(applicationVersion.getApplication()))
        .thenReturn(consentProductionFiguresViews);

    when(
        freeMarkerTemplateRenderingService.renderTemplate(
            "fcs/application/caseprocessing/document/instance/pdftemplate/consent/production/longTermProductionConsentSchedule.ftl",
            Map.of(
                "capitalizedConsentLengthType",
                WordUtils.capitalizeFully(ConsentLengthType.LONG_TERM.getShortDisplayName()),
                "primaryFieldName",
                primaryFieldName,
                "productionFromDate",
                DateUtils.format(longTermProductionConsentProductionFromDate, DateUtils.LONG_DATE),
                "consentEndDate",
                consentEndDate,
                "consentProductionFiguresViews",
                consentProductionFiguresViews
            )
        )
    ).thenReturn(html);

    assertThat(scheduleMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(html));
  }

  @ParameterizedTest
  @EnumSource(
      value = DocumentTemplateType.class,
      names = { "FIELD_FLARE_CONSENT", "TERMINAL_FLARE_CONSENT", "FIELD_VENT_CONSENT", "TERMINAL_VENT_CONSENT" },
      mode = EnumSource.Mode.INCLUDE
  )
  void resolve_documentTemplateTypeIsFieldFlareConsentOrTerminalFlareConsentOrFieldVentConsentOrTerminalVentConsent(
      DocumentTemplateType documentTemplateType
  ) throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(
            DocumentTemplateDtoTestUtil.builder()
                .withMnemonic(documentTemplateType.getMnemonic())
                .build()
        )
        .build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var emissionDailyAverage = BigDecimal.valueOf(235.79);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withEmissionDailyAverage(emissionDailyAverage)
        .build();

    var consentStartDate = "17/01/2024";
    var consentEndDate = "17/01/2024";

    var html = "<html></html>";

    when(applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto)).thenReturn(applicationVersion);
    when(consentDataService.getConsentData(applicationVersion.getApplication())).thenReturn(consentData);
    when(consentStartDateMailMergeField.resolve(documentInstanceDto)).thenReturn(DocumentMailMergeFieldResolveResult.success(consentStartDate));
    when(consentEndDateMailMergeField.resolve(documentInstanceDto)).thenReturn(DocumentMailMergeFieldResolveResult.success(consentEndDate));

    when(
        freeMarkerTemplateRenderingService.renderTemplate(
            "fcs/application/caseprocessing/document/instance/pdftemplate/consent/emission/emissionConsentSchedule.ftl",
            Map.of(
                "consentStartDate",
                consentStartDate,
                "consentEndDate",
                consentEndDate,
                "emissionDailyAverage",
                bigDecimalToFormattedString(emissionDailyAverage)
            )
        )
    ).thenReturn(html);

    assertThat(scheduleMailMergeField.resolve(documentInstanceDto))
        .isEqualTo(DocumentMailMergeFieldResolveResult.success(html));
  }
}
