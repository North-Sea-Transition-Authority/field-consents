package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.util.StringUtil;

@Order(8)
@Component
class LicenceReferenceListMailMergeField implements DocumentMailMergeField {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final ApplicationAssetLicenceService applicationAssetLicenceService;

  @Autowired
  LicenceReferenceListMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      ApplicationAssetLicenceService applicationAssetLicenceService
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.applicationAssetLicenceService = applicationAssetLicenceService;
  }

  @Override
  public String getMnemonic() {
    return "LICENCE_REFERENCE_LIST";
  }

  @Override
  public String getDescription() {
    return "A list of the licence references associated with the application";
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

    var licenceReferences = applicationAssetLicenceService.getAssetLicences(applicationVersion).stream()
        .map(ApplicationAssetLicence::getCachedLicenceRef)
        .distinct()
        .toList();

    return StringUtil.formatStringList(licenceReferences);
  }
}
