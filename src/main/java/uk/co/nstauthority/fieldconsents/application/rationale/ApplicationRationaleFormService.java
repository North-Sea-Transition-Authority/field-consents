package uk.co.nstauthority.fieldconsents.application.rationale;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.rationale.flare.ApplicationRationaleFlareForm;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;

@Service
public class ApplicationRationaleFormService {

  private final AssetService assetService;

  ApplicationRationaleFormService(AssetService assetService) {
    this.assetService = assetService;
  }

  public List<ApplicationAssetView> getFlaringLocationsFromForm(ApplicationRationaleFlareForm form) {
    return form.flaringLocationAssetKeys()
        .stream()
        .map(AssetKey::parse)
        .flatMap(Optional::stream)
        .map(assetKey -> assetService.getAsset(assetKey, "prefilling flaring locations for application rationale"))
        .flatMap(Optional::stream)
        .map(ApplicationAssetView::from)
        .toList();
  }

  public RestSearchItem getHostLocationFormForm(ApplicationRationaleFlareForm form) {
    return AssetKey.parse(form.hostLocationAssetKey())
        .flatMap(assetKey -> assetService.getAsset(assetKey, "prefilling host location for application rationale"))
        .map(RestSearchItem::from)
        .orElse(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

}
