package uk.co.nstauthority.fieldconsents.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

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

  public SummaryCard getApplicationContextSummaryCard(ApplicationVersion applicationVersion) {
    var applicationContextJson = getApplicationContextJson(applicationVersion);

    var summaryData = SummaryDataView
        .newWithKeyValue("Application type", applicationVersion.getApplication().getType().getDisplayName())
        .addKeyValue(applicationContextJson.getPrimaryAssetPrompt(), applicationContextJson.getPrimaryAssetName())
        .addKeyValue("Primary operator", applicationContextJson.getPrimaryOperatorName());

    return SummaryCard.simpleSummaryCard(summaryData);
  }
}
