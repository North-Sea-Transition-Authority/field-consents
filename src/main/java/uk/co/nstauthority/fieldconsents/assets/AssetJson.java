package uk.co.nstauthority.fieldconsents.assets;

import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public interface AssetJson extends SearchSelectable {

  Integer getId();

  String getName();

  String getStatusDisplayName();

  AssetType getAssetType();

  default AssetKey getAssetKey() {
    return new AssetKey(getId(), getAssetType());
  }

  @Override
  default String getSelectionId() {
    return getAssetKey().toString();
  }

  @Override
  default String getSelectionText() {
    return getName();
  }
}
