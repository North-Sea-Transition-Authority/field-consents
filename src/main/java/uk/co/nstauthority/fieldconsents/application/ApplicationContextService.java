package uk.co.nstauthority.fieldconsents.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
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
    AssetJson primaryAsset = applicationAssetService.getAssetJsonForApplicationAsset(
        applicationAssetService.getPrimaryApplicationAsset(applicationVersion));

    // primary operator
    OrganisationUnitJson primaryOperator =
        organisationUnitService.findOrganisationUnitById(applicationVersion.getPrimaryOperatorOuId(),
            "Organisation lookup for application context information")
            .orElseGet(() -> OrganisationUnitJson.fromCachedInformation(applicationVersion.getPrimaryOperatorOuId(),
                applicationVersion.getCachedPrimaryOperatorName()));

    return new ApplicationContextJson(primaryAsset, primaryOperator);
  }
}
