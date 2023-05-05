package uk.co.nstauthority.fieldconsents.application;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;
import uk.co.nstauthority.fieldconsents.util.enumutil.DisplayableEnumOptionUtil;

public enum ApplicationType implements Displayable {

  PRODUCTION("Production", 1, EnumSet.of(AssetType.FIELD), "PCON"),
  FLARE("Flare", 2, EnumSet.of(AssetType.FIELD, AssetType.TERMINAL), "FCON"),
  VENT("Vent", 3, EnumSet.of(AssetType.FIELD, AssetType.TERMINAL), "VCON");


  private final String displayName;

  private final int displayOrder;

  private final EnumSet<AssetType> assetTypes;

  private final String referenceMnemonic;

  ApplicationType(String displayName, int displayOrder, EnumSet<AssetType> assetTypes, String referenceMnemonic) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.assetTypes = assetTypes;
    this.referenceMnemonic = referenceMnemonic;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public EnumSet<AssetType> getAssetTypes() {
    return assetTypes;
  }

  public String getReferenceMnemonic() {
    return referenceMnemonic;
  }

  public static LinkedHashSet<ApplicationType> getForAssetType(AssetType assetType) {
    return Arrays.stream(ApplicationType.values())
        .filter(type -> type.getAssetTypes().contains(assetType))
        .sorted(Comparator.comparing(ApplicationType::getDisplayOrder))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public static Map<String, String> getDisplayableOptions() {
    return DisplayableEnumOptionUtil.getDisplayableOptions(ApplicationType.class);
  }
}
