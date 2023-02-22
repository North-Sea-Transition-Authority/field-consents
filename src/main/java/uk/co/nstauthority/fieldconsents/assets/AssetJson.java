package uk.co.nstauthority.fieldconsents.assets;

import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public interface AssetJson extends SearchSelectable {

  Integer getId();

  String getName();

  String getStatusDisplayName();

  AssetType getAssetType();

  @Override
  default String getSelectionId() {
    return getId().toString() + getAssetType().name();
  }

  @Override
  default String getSelectionText() {
    return getName();
  }
}
