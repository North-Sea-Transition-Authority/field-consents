package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.document.lib.FreeMarkerTemplateRenderingService;

@ExtendWith(MockitoExtension.class)
class ScheduleMailMergeFieldTest {

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

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
    assertThat(scheduleMailMergeField.getDescription()).isEqualTo("The schedule for the consent");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(scheduleMailMergeField.isApplicable(template)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = DocumentTemplateType.class, names = {"FIELD_PRODUCTION_CONSENT"}, mode = EnumSource.Mode.EXCLUDE)
  void resolve_documentTemplateTypeIsNotConsent(DocumentTemplateType documentTemplateType) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(
            DocumentTemplateDtoTestUtil.builder()
                .withMnemonic(documentTemplateType.getMnemonic())
                .build()
        )
        .build();

    assertThatThrownBy(() -> scheduleMailMergeField.resolve(documentInstanceDto))
        .isInstanceOf(MailMergeFieldFailedToResolveException.class);
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.INCLUDE)
  void resolve_consentLengthTypeIsShortTermOrAnnual(ConsentLengthType consentLengthType) throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(
            DocumentTemplateDtoTestUtil.builder()
                .withMnemonic(DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic())
                .build()
        )
        .build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var primaryFieldName = "Test primary field name";
    var consentStartDate = "17/01/2024";
    var consentEndDate = "17/01/2024";

    var html = "<html></html>";

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    when(primaryFieldNameMailMergeField.resolve(documentInstanceDto)).thenReturn(primaryFieldName);
    when(consentStartDateMailMergeField.resolve(documentInstanceDto)).thenReturn(consentStartDate);
    when(consentEndDateMailMergeField.resolve(documentInstanceDto)).thenReturn(consentEndDate);

    when(
        freeMarkerTemplateRenderingService.renderTemplate(
            "fcs/document/template/consent/production/shortTermOrAnnualProductionConsentSchedule.ftl",
            Map.of(
                "capitalizedConsentLengthType", WordUtils.capitalizeFully(consentLengthType.getShortDisplayName()),
                "primaryFieldName", primaryFieldName,
                "consentStartDate", consentStartDate,
                "consentEndDate", consentEndDate
            )
        )
    ).thenReturn(html);

    assertThat(scheduleMailMergeField.resolve(documentInstanceDto)).isEqualTo(html);
  }

  @Test
  void resolve_consentLengthTypeIsLongTerm() throws Exception {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(
            DocumentTemplateDtoTestUtil.builder()
                .withMnemonic(DocumentTemplateType.FIELD_PRODUCTION_CONSENT.getMnemonic())
                .build()
        )
        .build();

    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);

    var primaryFieldName = "Test primary field name";
    var consentStartDate = "17/01/2024";
    var consentEndDate = "17/01/2024";

    var html = "<html></html>";

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    when(primaryFieldNameMailMergeField.resolve(documentInstanceDto)).thenReturn(primaryFieldName);
    when(consentStartDateMailMergeField.resolve(documentInstanceDto)).thenReturn(consentStartDate);
    when(consentEndDateMailMergeField.resolve(documentInstanceDto)).thenReturn(consentEndDate);

    when(
        freeMarkerTemplateRenderingService.renderTemplate(
            "fcs/document/template/consent/production/longTermProductionConsentSchedule.ftl",
            Map.of(
                "capitalizedConsentLengthType", WordUtils.capitalizeFully(ConsentLengthType.LONG_TERM.getShortDisplayName()),
                "primaryFieldName", primaryFieldName,
                "consentStartDate", consentStartDate,
                "consentEndDate", consentEndDate
            )
        )
    ).thenReturn(html);

    assertThat(scheduleMailMergeField.resolve(documentInstanceDto)).isEqualTo(html);
  }
}
