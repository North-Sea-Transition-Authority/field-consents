package uk.co.nstauthority.fieldconsents.assets;

import com.google.common.annotations.VisibleForTesting;
import jakarta.validation.constraints.NotBlank;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class AssetSelectionForm {

  @NotBlank(message = "Select a field or facility")
  private String assetKey;

  private ApplicationVersion applicationVersion;

  public AssetSelectionForm() {
  }

  @VisibleForTesting
  public AssetSelectionForm(String assetKey, ApplicationVersion applicationVersion) {
    this.assetKey = assetKey;
    this.applicationVersion = applicationVersion;
  }

  public String getAssetKey() {
    return assetKey;
  }

  public void setAssetKey(String assetKey) {
    this.assetKey = assetKey;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  @Override
  public String toString() {
    return "AssetSelectionForm{" +
        "assetKey='" + assetKey + '\'' +
        '}';
  }
}
