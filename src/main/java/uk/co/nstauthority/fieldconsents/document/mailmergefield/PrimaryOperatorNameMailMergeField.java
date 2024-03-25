package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeField;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentMailMergeFieldResolveResult;
import uk.co.fivium.digitaldocumentlibrary.document.DocumentTemplateDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.ApplicationDocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Order(DocumentMailMergeFieldDisplayOrders.PRIMARY_OPERATOR_NAME)
@Component
class PrimaryOperatorNameMailMergeField implements DocumentMailMergeField {

  private final ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService;
  private final OrganisationUnitService organisationUnitService;

  PrimaryOperatorNameMailMergeField(
      ApplicationDocumentInstanceLinkingService applicationDocumentInstanceLinkingService,
      OrganisationUnitService organisationUnitService
  ) {
    this.applicationDocumentInstanceLinkingService = applicationDocumentInstanceLinkingService;
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
  public DocumentMailMergeFieldResolveResult resolve(DocumentInstanceDto documentInstanceDto) {
    var applicationVersion =
        applicationDocumentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto);

    var organisationUnitJson = organisationUnitService.getOrganisationUnitById(
        applicationVersion.getPrimaryOperatorOuId(),
        "Organisation unit lookup for %s mail merge field".formatted(getMnemonic())
    );

    return DocumentMailMergeFieldResolveResult.success(organisationUnitJson.name());
  }
}
