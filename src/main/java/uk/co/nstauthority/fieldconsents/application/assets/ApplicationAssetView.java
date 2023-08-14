package uk.co.nstauthority.fieldconsents.application.assets;

import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.fds.AddToListItem;

public record ApplicationAssetView(
    String assetKey,
    String displayName,
    boolean isValid
) implements AddToListItem {

  public static ApplicationAssetView from(AssetJson assetJson) {
    return new ApplicationAssetView(
        assetJson.getSelectionId(),
        assetJson.getSelectionText(),
        true
    );
  }

  @Override
  public String getId() {
    return assetKey;
  }

  @Override
  public String getName() {
    return displayName;
  }

}
