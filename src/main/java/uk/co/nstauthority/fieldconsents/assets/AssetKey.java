package uk.co.nstauthority.fieldconsents.assets;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public record AssetKey(Integer assetId, AssetType assetType) {

  public static AssetKey from(AssetJson assetJson) {
    return new AssetKey(assetJson.getId(), assetJson.getAssetType());
  }

  public static AssetKey from(String str) {
    return parse(str).orElseThrow(() -> new IllegalArgumentException("Invalid assetKey [%s]".formatted(str)));
  }

  public static Optional<AssetKey> parse(String str) {
    if (Objects.isNull(str)) {
      return Optional.empty();
    }

    var assetTypeOptional = Arrays.stream(AssetType.values())
        .filter(type -> str.endsWith(type.name()))
        .findFirst();

    if (assetTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    var assetType = assetTypeOptional.get();

    var maybeAssetId = str.replaceFirst(assetType.name(), "");
    var isInteger = !maybeAssetId.isBlank() && maybeAssetId.codePoints().allMatch(Character::isDigit);
    if (!isInteger) {
      return Optional.empty();
    }

    return Optional.of(new AssetKey(Integer.parseInt(maybeAssetId), assetType));
  }

}
