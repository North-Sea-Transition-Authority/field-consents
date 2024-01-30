package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.lib.FreeMarkerTemplateRenderingService;

@Order(10)
@Component
class ScheduleMailMergeField implements DocumentMailMergeField {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ConsentLengthService consentLengthService;
  private final FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;
  private final PrimaryFieldNameMailMergeField primaryFieldNameMailMergeField;
  private final ConsentStartDateMailMergeField consentStartDateMailMergeField;
  private final ConsentEndDateMailMergeField consentEndDateMailMergeField;

  @Autowired
  ScheduleMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ConsentLengthService consentLengthService,
      FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService,
      PrimaryFieldNameMailMergeField primaryFieldNameMailMergeField,
      ConsentStartDateMailMergeField consentStartDateMailMergeField,
      ConsentEndDateMailMergeField consentEndDateMailMergeField
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
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

    return DocumentTemplateType.isConsent(documentTemplateType);
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    String templateName;

    Map<String, String> model = new HashMap<>();
    model.put("consentStartDate", consentStartDateMailMergeField.resolve(documentInstanceDto));
    model.put("consentEndDate", consentEndDateMailMergeField.resolve(documentInstanceDto));

    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentInstanceDto.documentTemplateDto().mnemonic());

    switch (documentTemplateType) {
      case FIELD_PRODUCTION_CONSENT:
        var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();

        templateName = switch (consentLengthType) {
          case SHORT_TERM, ANNUAL ->
              "fcs/document/template/consent/production/shortTermOrAnnualProductionConsentSchedule.ftl";
          case LONG_TERM -> "fcs/document/template/consent/production/longTermProductionConsentSchedule.ftl";
        };

        model.put("capitalizedConsentLengthType", WordUtils.capitalizeFully(consentLengthType.getShortDisplayName()));
        model.put("primaryFieldName", primaryFieldNameMailMergeField.resolve(documentInstanceDto));
        break;
      case FIELD_FLARE_CONSENT, TERMINAL_FLARE_CONSENT:
        templateName = "fcs/document/template/consent/flare/flareConsentSchedule.ftl";
        break;
      case FIELD_VENT_CONSENT:
        templateName = "fcs/document/template/consent/vent/ventConsentSchedule.ftl";
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
