package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;

@Order(1)
@Component
class PrimaryFieldNameMailMergeField implements DocumentMailMergeField {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ApplicationAssetService applicationAssetService;

  @Autowired
  PrimaryFieldNameMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ApplicationAssetService applicationAssetService
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.applicationAssetService = applicationAssetService;
  }

  @Override
  public String getMnemonic() {
    return "PRIMARY_FIELD_NAME";
  }

  @Override
  public String getDescription() {
    return "The name of the primary field on the application";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return DocumentTemplateType.isField(documentTemplateType) && DocumentTemplateType.isConsent(documentTemplateType);
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    if (!primaryAsset.isField()) {
      throw new MailMergeFieldFailedToResolveException(
          "Primary asset type is not FIELD: %s".formatted(primaryAsset.getAssetType().name())
      );
    }

    return applicationAssetService.getAssetJsonForApplicationAsset(primaryAsset).getName();
  }
}
