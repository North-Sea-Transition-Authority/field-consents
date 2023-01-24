package uk.co.nstauthority.fieldconsents.application;

import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

public record ApplicationContextJson(
    AssetJson primaryAsset,
    OrganisationUnitJson primaryOperator
) {

  public String getPrimaryAssetPrompt() {
    return switch (primaryAsset.getAssetType()) {
      case FIELD -> "Primary field";
      case TERMINAL -> "Primary facility";
    };
  }

  public String getPrimaryAssetName() {
    return primaryAsset.getName();
  }

  public String getPrimaryOperatorName() {
    return primaryOperator.name();
  }
}
