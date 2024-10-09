package uk.co.nstauthority.fieldconsents.assets;

import jakarta.validation.constraints.NotBlank;
import java.util.Optional;

public record AssetSelectionForm(@NotBlank(message = "Select a field or facility") String assetKey) {

  public static AssetSelectionForm empty() {
    return new AssetSelectionForm(null);
  }

  public static AssetSelectionForm from(AssetKey assetKey) {
    return new AssetSelectionForm(assetKey.toString());
  }

  public Optional<AssetKey> getAssetKey() {
    return AssetKey.parse(assetKey);
  }
}
