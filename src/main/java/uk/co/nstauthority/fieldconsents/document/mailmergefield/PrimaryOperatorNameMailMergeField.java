package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Order(4)
@Component
class PrimaryOperatorNameMailMergeField implements DocumentMailMergeField {

  private final DocumentInstanceLinkingService documentInstanceLinkingService;
  private final OrganisationUnitService organisationUnitService;

  PrimaryOperatorNameMailMergeField(
      DocumentInstanceLinkingService documentInstanceLinkingService,
      OrganisationUnitService organisationUnitService
  ) {
    this.documentInstanceLinkingService = documentInstanceLinkingService;
    this.organisationUnitService = organisationUnitService;
  }

  @Override
  public String getMnemonic() {
    return "PRIMARY_OPERATOR_NAME";
  }

  @Override
  public String getDescription() {
    return "The name of the primary operator on the application";
  }

  @Override
  public boolean isApplicable(DocumentTemplateDto documentTemplateDto) {
    return true;
  }

  @Override
  public String resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var organisationUnitJson = organisationUnitService.getOrganisationUnitById(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit lookup for %s mail merge field".formatted(getMnemonic())
    );

    return organisationUnitJson.name();
  }
}

