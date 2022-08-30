package uk.co.nstauthority.fieldconsents.assets;

import javax.validation.constraints.NotBlank;

public class AssetSelectionForm {

  @NotBlank
  private String assetKey;

  public String getAssetKey() {
    return assetKey;
  }

  public void setAssetKey(String assetKey) {
    this.assetKey = assetKey;
  }

  @Override
  public String toString() {
    return "AssetSelectionForm{" +
        "assetKey='" + assetKey + '\'' +
        '}';
  }
}
