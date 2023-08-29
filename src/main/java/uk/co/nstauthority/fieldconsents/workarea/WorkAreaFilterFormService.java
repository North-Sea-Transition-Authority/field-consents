package uk.co.nstauthority.fieldconsents.workarea;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;

@Service
public class WorkAreaFilterFormService {

  public static final String ORGANISATION_UNIT_LOOKUP_PURPOSE = "Lookup organisation unit for the work-area";
  public static final String ASSETS_LOOKUP_PURPOSE = "Look up assets for work-area";

  private final AssetService assetService;

  private final ApplicationDataFilterFormService applicationDataFilterFormService;

  public WorkAreaFilterFormService(AssetService assetService,
                                   ApplicationDataFilterFormService applicationDataFilterFormService) {
    this.assetService = assetService;
    this.applicationDataFilterFormService = applicationDataFilterFormService;
  }

  public WorkAreaFilterForm getFromFilter(WorkAreaFilter filter) {
    var form = new WorkAreaFilterForm();
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
    return applicationDataFilterFormService.getPrefilledOrganisation(operatorId, ORGANISATION_UNIT_LOOKUP_PURPOSE);
  }

  public RestSearchItem getPrefilledAsset(String assetKey) {
    return AssetKey.parse(assetKey)
        .flatMap(key -> assetService.getAsset(AssetKey.from(assetKey), ASSETS_LOOKUP_PURPOSE))
        .map(RestSearchItem::from)
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }
}
