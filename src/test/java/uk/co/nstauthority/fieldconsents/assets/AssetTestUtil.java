package uk.co.nstauthority.fieldconsents.assets;

import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil;

class AssetTestUtil {
  static AssetJson field1AssetJson = AssetJson.from(FieldTestUtil.field1Json);
  static final String FIELD1_ASSET_KEY = field1AssetJson.assetId() + field1AssetJson.assetType().name();
  static AssetJson terminal1AssetJson = AssetJson.from(TerminalTestUtil.terminal1Json);
  static final String TERMINAL1_ASSET_KEY = terminal1AssetJson.assetId() + terminal1AssetJson.assetType().name();
  static final String BAD_ASSET_KEY = "BADASSETKEY";
}
