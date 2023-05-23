package uk.co.nstauthority.fieldconsents.workarea;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@Service
public class WorkAreaFormService {

  public static final String FIELD_LOOKUP_PURPOSE = "Lookup field for the work-area";

  public static final String ORGANISATION_UNIT_LOOKUP_PURPOSE = "Lookup organisation unit for the work-area";

  private final AssetService assetService;

  private final OrganisationUnitService organisationUnitService;

  public WorkAreaFormService(AssetService assetService,
                             OrganisationUnitService organisationUnitService) {
    this.assetService = assetService;
    this.organisationUnitService = organisationUnitService;
  }

  public WorkAreaForm getFromFilter(WorkAreaFilter filter) {
    var form = new WorkAreaForm();
    form.setReferenceNumber(filter.getReferenceNumber());
    form.setStatuses(filter.getStatuses());
    form.setApplicationTypes(filter.getApplicationTypes());
    form.setDurationTypes(filter.getDurationTypes());
    form.setAssetKey(filter.getAssetKey());
    form.setOperatorId(filter.getOperatorId());
    form.setGeographicAreas(filter.getGeographicAreas());
    form.setAssetTypesWithShore(filter.getAssetTypesWithShore());
    return form;
  }

  public RestSearchItem getPrefilledOrganisation(Integer operatorId) {
    if (operatorId == null) {
      return RestSearchItem.EMPTY_REST_SEARCH_ITEM;
    }

    return organisationUnitService.findOrganisationUnitById(operatorId, ORGANISATION_UNIT_LOOKUP_PURPOSE)
        .map(organisationUnitJson ->
            new RestSearchItem(organisationUnitJson.getSelectionId(), organisationUnitJson.getSelectionText()))
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  public RestSearchItem getPrefilledAsset(String assetKey) {
    return assetService.getAssetFromKey(assetKey)
        .map(assetJson -> new RestSearchItem(assetJson.getSelectionId(), assetJson.getSelectionText()))
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }
}
