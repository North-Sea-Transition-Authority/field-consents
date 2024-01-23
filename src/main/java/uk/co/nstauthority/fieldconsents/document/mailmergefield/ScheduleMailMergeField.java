package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.lib.FreeMarkerTemplateRenderingService;

@Order(6)
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
    return "The schedule for the consent";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return DocumentTemplateType.isConsent(DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic()));
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentInstanceDto.documentTemplateDto().mnemonic());
    if (documentTemplateType != DocumentTemplateType.FIELD_PRODUCTION_CONSENT) {
      throw new MailMergeFieldFailedToResolveException(
          "Unsupported DocumentTemplateType: %s".formatted(documentTemplateType)
      );
    }

    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var consentLengthType = consentLengthService.getConsentLengthDetails(applicationVersion).getConsentLength();
    if (consentLengthType != ConsentLengthType.SHORT_TERM && consentLengthType != ConsentLengthType.ANNUAL) {
      throw new MailMergeFieldFailedToResolveException(
          "Unsupported ConsentLengthType: %s".formatted(consentLengthType)
      );
    }

    var model = Map.of(
        "capitalizedConsentLengthType", WordUtils.capitalizeFully(consentLengthType.getShortDisplayName()),
        "primaryFieldName", primaryFieldNameMailMergeField.resolve(documentInstanceDto),
        "consentStartDate", consentStartDateMailMergeField.resolve(documentInstanceDto),
        "consentEndDate", consentEndDateMailMergeField.resolve(documentInstanceDto)
    );

    try {
      return freeMarkerTemplateRenderingService.renderTemplate(
          "fcs/document/template/consent/production/shortTermOrAnnualProductionConsentSchedule.ftl",
          model
      );
    } catch (Exception exception) {
      throw new MailMergeFieldFailedToResolveException(
          "Exception rendering schedule for document instance: %s".formatted(documentInstanceDto.id()),
          exception
      );
    }
  }
}
