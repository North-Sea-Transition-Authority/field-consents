package uk.co.nstauthority.fieldconsents.assets;

import java.util.Map;
import uk.co.nstauthority.fieldconsents.assets.fields.Shore;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum AssetTypeWithShore implements Displayable {

  TERMINAL("Facility", 1, AssetType.TERMINAL, null),
  FIELD_OFFSHORE("Field - offshore", 2, AssetType.FIELD, Shore.OFFSHORE),
  FIELD_ONSHORE("Field - onshore", 3, AssetType.FIELD, Shore.ONSHORE),
  FIELD_UNKNOWN("Field - unknown shore", 4, AssetType.FIELD, Shore.UNKNOWN);

  private final String displayName;

  private final int displayOrder;

  private final AssetType assetType;

  private final Shore shore;

  AssetTypeWithShore(String displayName, int displayOrder, AssetType assetType, Shore shore) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.assetType = assetType;
    this.shore = shore;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public AssetType getAssetType() {
    return assetType;
  }

  public Shore getShore() {
    return shore;
  }

  public static Map<String, String> getDisplayableOptions() {
    return DisplayableEnumOptionUtil.getDisplayableOptions(AssetTypeWithShore.class);
  }

  public boolean isField() {
    return this.assetType.equals(AssetType.FIELD);
  }

  public boolean isTerminal() {
    return this.assetType.equals(AssetType.TERMINAL);
  }
}
