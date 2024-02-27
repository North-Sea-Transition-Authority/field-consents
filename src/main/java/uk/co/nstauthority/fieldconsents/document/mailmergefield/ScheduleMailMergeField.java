package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionLongTermFiguresService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.lib.FreeMarkerTemplateRenderingService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Order(18)
@Component
class ScheduleMailMergeField implements DocumentMailMergeField {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ConsentDataService consentDataService;
  private final ConsentProductionLongTermFiguresService consentProductionLongTermFiguresService;
  private final ConsentLengthService consentLengthService;
  private final FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;
  private final PrimaryFieldNameMailMergeField primaryFieldNameMailMergeField;
  private final ConsentStartDateMailMergeField consentStartDateMailMergeField;
  private final ConsentEndDateMailMergeField consentEndDateMailMergeField;

  ScheduleMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ConsentDataService consentDataService,
      ConsentProductionLongTermFiguresService consentProductionLongTermFiguresService,
      ConsentLengthService consentLengthService,
      FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService,
      PrimaryFieldNameMailMergeField primaryFieldNameMailMergeField,
      ConsentStartDateMailMergeField consentStartDateMailMergeField,
      ConsentEndDateMailMergeField consentEndDateMailMergeField
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.consentDataService = consentDataService;
    this.consentProductionLongTermFiguresService = consentProductionLongTermFiguresService;
    this.consentLengthService = consentLengthService;
    this.freeMarkerTemplateRenderingService = freeMarkerTemplateRenderingService;
    this.primaryFieldNameMailMergeField = primaryFieldNameMailMergeField;
    this.consentStartDateMailMergeField = consentStartDateMailMergeField;
    this.consentEndDateMailMergeField = consentEndDateMailMergeField;
  }

  @Override
  public String getMnemonic() {
    return "SCHEDULE";
  }

  @Override
  public String getDescription() {
    return "The schedule for the Consent";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return documentTemplateType.isConsent();
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);
    var application = applicationVersion.getApplication();

    String templateName;
    Map<String, Object> model = new HashMap<>();

    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentInstanceDto.documentTemplateDto().mnemonic());
    var consentData = consentDataService.getConsentData(application);

    switch (documentTemplateType) {
      case FIELD_PRODUCTION_CONSENT:
        var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

        model.put("capitalizedConsentLengthType", WordUtils.capitalizeFully(consentLengthType.getShortDisplayName()));
        model.put("primaryFieldName", primaryFieldNameMailMergeField.resolve(documentInstanceDto));

        switch (consentLengthType) {
          case SHORT_TERM, ANNUAL:
            templateName = "fcs/document/template/consent/production/shortTermOrAnnualProductionConsentSchedule.ftl";

            model.put("consentStartDate", consentStartDateMailMergeField.resolve(documentInstanceDto));
            model.put("consentEndDate", consentEndDateMailMergeField.resolve(documentInstanceDto));

            var consentProductionFiguresView =
                ConsentProductionFiguresView.fromShortTermOrAnnualConsentProductionFigures(consentData);
            model.put("consentProductionFiguresView", consentProductionFiguresView);
            break;
          case LONG_TERM:
            templateName = "fcs/document/template/consent/production/longTermProductionConsentSchedule.ftl";

            var scheduleStartDate =
                DateUtils.format(consentData.getLongTermProductionConsentScheduleStartDate(), DateUtils.LONG_DATE);
            model.put("scheduleStartDate", scheduleStartDate);
            model.put("consentEndDate", consentEndDateMailMergeField.resolve(documentInstanceDto));

            var consentProductionFiguresViews =
                consentProductionLongTermFiguresService.getConsentProductionLongTermFiguresViews(application);
            model.put("consentProductionFiguresViews", consentProductionFiguresViews);
            break;
          default:
            throw new IllegalStateException("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
        }
        break;
      case FIELD_FLARE_CONSENT, TERMINAL_FLARE_CONSENT, FIELD_VENT_CONSENT, TERMINAL_VENT_CONSENT:
        templateName = "fcs/document/template/consent/emission/emissionConsentSchedule.ftl";

        model.put("consentStartDate", consentStartDateMailMergeField.resolve(documentInstanceDto));
        model.put("consentEndDate", consentEndDateMailMergeField.resolve(documentInstanceDto));

        var emissionDailyAverage = bigDecimalToFormattedString(consentData.getEmissionDailyAverage());
        model.put("emissionDailyAverage", emissionDailyAverage);
        break;
      default:
        throw new MailMergeFieldFailedToResolveException(
            "Unsupported DocumentTemplateType: %s".formatted(documentTemplateType)
        );
    }

    try {
      return freeMarkerTemplateRenderingService.renderTemplate(templateName, model);
    } catch (Exception exception) {
      throw new MailMergeFieldFailedToResolveException(
          "Exception rendering schedule for document instance: %s".formatted(documentInstanceDto.id()),
          exception
      );
    }
  }
}
