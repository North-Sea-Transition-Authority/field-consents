package uk.co.nstauthority.fieldconsents.assets;

import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil;

class AssetTestUtil {
  static AssetJson field1AssetJson = FieldTestUtil.field1Json;
  static final String FIELD1_ASSET_KEY = field1AssetJson.getId() + field1AssetJson.getAssetType().name();
  static AssetJson terminal1AssetJson = TerminalTestUtil.terminal1Json;
  static final String TERMINAL1_ASSET_KEY = terminal1AssetJson.getId() + terminal1AssetJson.getAssetType().name();
  static final String BAD_ASSET_KEY = "BADASSETKEY";
}
