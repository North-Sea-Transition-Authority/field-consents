package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.fivium.digitaldocumentlibrary.document.FreeMarkerTemplateRenderingService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Order(DocumentMailMergeFieldDisplayOrders.SCHEDULE)
@Component
class ScheduleMailMergeField implements DocumentMailMergeField {

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final ConsentDataService consentDataService;
  private final ConsentDataLongTermProductionFiguresService consentDataLongTermProductionFiguresService;
  private final ConsentLengthService consentLengthService;
  private final FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;
  private final PrimaryFieldNameMailMergeField primaryFieldNameMailMergeField;
  private final ConsentStartDateMailMergeField consentStartDateMailMergeField;
  private final ConsentEndDateMailMergeField consentEndDateMailMergeField;

  ScheduleMailMergeField(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      ConsentDataService consentDataService,
      ConsentDataLongTermProductionFiguresService consentDataLongTermProductionFiguresService,
      ConsentLengthService consentLengthService,
      FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService,
      PrimaryFieldNameMailMergeField primaryFieldNameMailMergeField,
      ConsentStartDateMailMergeField consentStartDateMailMergeField,
      ConsentEndDateMailMergeField consentEndDateMailMergeField
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.consentDataService = consentDataService;
    this.consentDataLongTermProductionFiguresService = consentDataLongTermProductionFiguresService;
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
    return "The schedule for the consent";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic()).isConsent();
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var scheduleContent = getScheduleContent(documentInstanceDto);
    return DocumentMailMergeFieldResolveResult.success(scheduleContent);
  }

  private String getScheduleContent(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);
    var application = applicationVersion.getApplication();

    String templateName;
    Map<String, Object> model = new HashMap<>();

    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentInstanceDto.documentTemplateDto().mnemonic());
    var consentData = consentDataService.getConsentData(application);

    switch (documentTemplateType) {
      case FIELD_PRODUCTION_CONSENT:
        var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

        model.put("capitalizedConsentLengthType", WordUtils.capitalizeFully(consentLengthType.getShortDisplayName()));
        model.put("primaryFieldName", primaryFieldNameMailMergeField.resolve(documentInstanceDto).resolvedValueOrThrow());

        switch (consentLengthType) {
          case SHORT_TERM, ANNUAL:
            templateName = "fcs/application/caseprocessing/document/instance/pdftemplate/consent/production/" +
                "shortTermOrAnnualProductionConsentSchedule.ftl";

            model.put("consentStartDate", consentStartDateMailMergeField.resolve(documentInstanceDto).resolvedValueOrThrow());
            model.put("consentEndDate", consentEndDateMailMergeField.resolve(documentInstanceDto).resolvedValueOrThrow());

            var consentProductionFiguresView =
                ConsentProductionFiguresView.fromShortTermOrAnnualConsentProductionFigures(consentData);
            model.put("consentProductionFiguresView", consentProductionFiguresView);
            break;
          case LONG_TERM:
            templateName = "fcs/application/caseprocessing/document/instance/pdftemplate/consent/production/" +
                "longTermProductionConsentSchedule.ftl";

            var productionFromDate =
                DateUtils.format(consentData.getLongTermProductionConsentProductionFromDate(), DateUtils.LONG_DATE);
            model.put("productionFromDate", productionFromDate);
            model.put("consentEndDate", consentEndDateMailMergeField.resolve(documentInstanceDto).resolvedValueOrThrow());

            var consentProductionFiguresViews =
                consentDataLongTermProductionFiguresService.getConsentDataLongTermProductionFiguresViews(application);
            model.put("consentProductionFiguresViews", consentProductionFiguresViews);
            break;
          default:
            throw new IllegalStateException("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
        }
        break;
      case FIELD_FLARE_CONSENT, TERMINAL_FLARE_CONSENT, FIELD_VENT_CONSENT, TERMINAL_VENT_CONSENT:
        templateName = "fcs/application/caseprocessing/document/instance/pdftemplate/consent/emission/" +
            "emissionConsentSchedule.ftl";

        model.put("consentStartDate", consentStartDateMailMergeField.resolve(documentInstanceDto).resolvedValueOrThrow());
        model.put("consentEndDate", consentEndDateMailMergeField.resolve(documentInstanceDto).resolvedValueOrThrow());

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
