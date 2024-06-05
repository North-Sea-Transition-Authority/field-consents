package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FormattedFieldEquityPartner;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.util.StringUtil;

@Order(DocumentMailMergeFieldDisplayOrders.FIELD_EQUITY_PARTNER_LIST)
@Component
public class FieldEquityPartnerListMailMergeField implements DocumentMailMergeField {

  private static final String MNEMONIC = "FIELD_EQUITY_PARTNER_LIST";
  static final String DESCRIPTION = """
      A list of field equity partners associated to the fields on this application. \
      Includes the organisation name and registered number.\
      """;

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final FieldEquityPartnerService fieldEquityPartnerService;

  FieldEquityPartnerListMailMergeField(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      FieldEquityPartnerService fieldEquityPartnerService
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.fieldEquityPartnerService = fieldEquityPartnerService;
  }

  @Override
  public String getMnemonic() {
    return MNEMONIC;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return documentTemplateType.isApplicableToFieldApplications();
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(
            documentInstanceDto);

    var formattedFieldEquityPartners = fieldEquityPartnerService.getFormattedFieldEquityPartners(applicationVersion);

    var formattedFieldEquityPartnerValues = formattedFieldEquityPartners
        .stream()
        .map(FormattedFieldEquityPartner::getFormattedValue)
        .toList();

    return DocumentMailMergeFieldResolveResult.success(StringUtil.formatStringList(formattedFieldEquityPartnerValues));
  }
}
