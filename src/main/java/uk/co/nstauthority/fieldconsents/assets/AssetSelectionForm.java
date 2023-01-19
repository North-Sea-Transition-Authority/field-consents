package uk.co.nstauthority.fieldconsents.assets;

import com.google.common.annotations.VisibleForTesting;
import javax.validation.constraints.NotBlank;

public class AssetSelectionForm {

  @NotBlank
  private String assetKey;

  public AssetSelectionForm() {
  }

  @VisibleForTesting
  public AssetSelectionForm(String assetKey) {
    this.assetKey = assetKey;
  }

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
