package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

@Order(3)
@Component
class FacilityNameMailMergeField implements DocumentMailMergeField {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ApplicationAssetService applicationAssetService;
  private final TerminalService terminalService;

  @Autowired
  FacilityNameMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ApplicationAssetService applicationAssetService,
      TerminalService terminalService
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.applicationAssetService = applicationAssetService;
    this.terminalService = terminalService;
  }

  @Override
  public String getMnemonic() {
    return "FACILITY_NAME";
  }

  @Override
  public String getDescription() {
    return "The name of the facility on the application";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return DocumentTemplateType.isTerminal(documentTemplateType);
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    if (!primaryAsset.isTerminal()) {
      throw new MailMergeFieldFailedToResolveException(
          "Primary asset type is not TERMINAL: %s".formatted(primaryAsset.getAssetType().name())
      );
    }

    return terminalService.getTerminal(primaryAsset.getAssetId(), "Terminal lookup for application asset").getName();
  }
}
