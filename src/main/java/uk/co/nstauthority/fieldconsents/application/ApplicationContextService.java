package uk.co.nstauthority.fieldconsents.application;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroup;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

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

  public SummaryGroup<SummaryDataView> getApplicationContextSummaryGroup(ApplicationVersion applicationVersion) {
    var applicationContextJson = getApplicationContextJson(applicationVersion);

    List<SummaryKeyValue> summaryKeyValues = new ArrayList<>();

    summaryKeyValues.add(SummaryKeyValue.from("Application type",
        applicationVersion.getApplication().getType().getDisplayName()));
    summaryKeyValues.add(SummaryKeyValue.from(applicationContextJson.getPrimaryAssetPrompt(),
        applicationContextJson.getPrimaryAssetName()));
    summaryKeyValues.add(SummaryKeyValue.from("Primary operator",
        applicationContextJson.getPrimaryOperatorName()));

    return SummaryGroup.simpleSummaryGroup(summaryKeyValues);
  }
}
