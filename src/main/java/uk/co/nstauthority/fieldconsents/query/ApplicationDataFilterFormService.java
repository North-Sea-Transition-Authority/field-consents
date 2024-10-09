package uk.co.nstauthority.fieldconsents.query;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Service
public class ApplicationDataFilterFormService {

  public static final String ORGANISATION_UNIT_LOOKUP_PURPOSE = "Lookup organisation unit for application data";

  private final AssetService assetService;
  private final OrganisationUnitService organisationUnitService;

  public ApplicationDataFilterFormService(AssetService assetService,
                                          OrganisationUnitService organisationUnitService) {
    this.assetService = assetService;
    this.organisationUnitService = organisationUnitService;
  }

  public RestSearchItem getPrefilledOrganisation(Integer operatorId, String requestPurpose) {
    if (operatorId == null) {
      return RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    }

    return organisationUnitService.findOrganisationUnitById(operatorId, requestPurpose)
        .map(RestSearchItem::from)
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  public RestSearchItem getPrefilledOrganisation(Integer operatorId) {
    return getPrefilledOrganisation(operatorId, ORGANISATION_UNIT_LOOKUP_PURPOSE);
  }

  public RestSearchItem getPrefilledAsset(String assetKey) {
    return AssetKey.parse(assetKey)
        .flatMap(assetService::findAsset)
        .map(RestSearchItem::from)
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }
}
