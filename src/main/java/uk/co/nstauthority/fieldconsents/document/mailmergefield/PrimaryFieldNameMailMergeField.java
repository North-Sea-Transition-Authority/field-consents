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
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateType;

@Order(DocumentMailMergeFieldDisplayOrders.PRIMARY_FIELD_NAME)
@Component
class PrimaryFieldNameMailMergeField implements DocumentMailMergeField {

  static final String DESCRIPTION = "The name of the primary field on the application.";

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final ApplicationAssetService applicationAssetService;
  private final FieldService fieldService;

  @Autowired
  PrimaryFieldNameMailMergeField(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      ApplicationAssetService applicationAssetService,
      FieldService fieldService
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
    this.applicationAssetService = applicationAssetService;
    this.fieldService = fieldService;
  }

  @Override
  public String getMnemonic() {
    return "PRIMARY_FIELD_NAME";
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    var documentTemplateType = DocumentTemplateType.getByMnemonic(documentTemplateDto.mnemonic());

    return documentTemplateType == DocumentTemplateType.FIELD_PRODUCTION_CONSENT
        || documentTemplateType == DocumentTemplateType.FLARE_AND_COMMISSIONING_LETTER;
  }

  @Override
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var primaryAsset = applicationAssetService.getPrimaryAsset(applicationVersion);
    if (!primaryAsset.isField()) {
      throw new MailMergeFieldFailedToResolveException(
          "Primary asset type is not FIELD: %s".formatted(primaryAsset.getAssetType().name())
      );
    }

    var fieldJson =
        fieldService.getField(primaryAsset.getAssetId(), "Field lookup for %s mail merge field".formatted(getMnemonic()));

    return DocumentMailMergeFieldResolveResult.success(fieldJson.getName());
  }
}
