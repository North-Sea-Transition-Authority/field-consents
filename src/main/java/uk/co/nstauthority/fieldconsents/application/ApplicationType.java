package uk.co.nstauthority.fieldconsents.application;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

public enum ApplicationType {

  PRODUCTION("Production", 1, EnumSet.of(AssetType.FIELD)),
  FLARE("Flare", 2, EnumSet.of(AssetType.FIELD, AssetType.TERMINAL)),
  VENT("Vent", 3, EnumSet.of(AssetType.FIELD, AssetType.TERMINAL));


  private final String displayName;

  private final int displayOrder;

  private final EnumSet<AssetType> assetTypes;

  ApplicationType(String displayName, int displayOrder, EnumSet<AssetType> assetTypes) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.assetTypes = assetTypes;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public EnumSet<AssetType> getAssetTypes() {
    return assetTypes;
  }

  public static LinkedHashSet<ApplicationType> getForAssetType(AssetType assetType) {
    return Arrays.stream(ApplicationType.values())
        .filter(type -> type.getAssetTypes().contains(assetType))
        .sorted(Comparator.comparing(ApplicationType::getDisplayOrder))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
