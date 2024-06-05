package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@Order(DocumentMailMergeFieldDisplayOrders.FACILITY_NAME)
@Component
class FacilityNameMailMergeField implements DocumentMailMergeField {

  static final String DESCRIPTION = "The name of the facility on the application.";

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final ApplicationAssetService applicationAssetService;
  private final TerminalService terminalService;

  @Autowired
  FacilityNameMailMergeField(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      ApplicationAssetService applicationAssetService,
      TerminalService terminalService
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.applicationAssetService = applicationAssetService;
    this.terminalService = terminalService;
  }

  @Override
  public String getMnemonic() {
    return "FACILITY_NAME";
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return documentTemplateType.isApplicableToTerminalApplications();
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    if (!primaryAsset.isTerminal()) {
      throw new MailMergeFieldFailedToResolveException(
          "Primary asset type is not TERMINAL: %s".formatted(primaryAsset.getAssetType().name())
      );
    }

    var terminalJson = terminalService.getTerminal(
        primaryAsset.getAssetId(),
        "Terminal lookup for %s mail merge field".formatted(getMnemonic())
    );

    return DocumentMailMergeFieldResolveResult.success(terminalJson.getName());
  }
}
