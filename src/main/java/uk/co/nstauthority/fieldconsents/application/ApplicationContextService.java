package uk.co.nstauthority.fieldconsents.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Service
public class ApplicationContextService {

  private final ApplicationAssetService applicationAssetService;

  private final OrganisationUnitService organisationUnitService;

  @Autowired
  ApplicationContextService(ApplicationAssetService applicationAssetService,
                            OrganisationUnitService organisationUnitService) {
    this.applicationAssetService = applicationAssetService;
    this.organisationUnitService = organisationUnitService;
  }

  public ApplicationContextJson getApplicationContextJson(ApplicationVersion applicationVersion) {

    // primary asset
    var primaryAsset = applicationAssetService.getAssetJsonForApplicationAsset(
        applicationAssetService.getPrimaryAsset(applicationVersion));

    // primary operator
    OrganisationUnitJson primaryOperator =
        organisationUnitService.getOrganisationUnitByIdOrFallback(applicationVersion.getPrimaryOperatorOuId(),
            "Organisation lookup for application context information",
            applicationVersion.getCachedPrimaryOperatorName());

    return new ApplicationContextJson(primaryAsset, primaryOperator);
  }
}
