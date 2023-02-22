package uk.co.nstauthority.fieldconsents.assets;

import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil;

public class AssetTestUtil {
  public static AssetJson field1AssetJson = FieldTestUtil.field1Json;
  public static AssetJson field2AssetJson = FieldTestUtil.field2Json;
  public static AssetJson terminal1AssetJson = TerminalTestUtil.terminal1Json;

  public static final String FIELD1_ASSET_KEY = field1AssetJson.getId() + field1AssetJson.getAssetType().name();
  public static final String FIELD2_ASSET_KEY = field2AssetJson.getId() + field2AssetJson.getAssetType().name();
  public static final String TERMINAL1_ASSET_KEY = terminal1AssetJson.getId() + terminal1AssetJson.getAssetType().name();
  public static final String BAD_ASSET_KEY = "BADASSETKEY";
}
