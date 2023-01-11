package uk.co.nstauthority.fieldconsents.assets;

import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public record AssetJson(
    @NotNull Integer assetId,
    @NotNull String assetName,
    @NotNull AssetType assetType
) implements SearchSelectable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssetJson.class);

  public static AssetJson from(FieldJson fieldJson) {
    return new AssetJson(fieldJson.fieldId(), fieldJson.fieldName(), AssetType.FIELD);
  }

  public static AssetJson from(TerminalJson terminalJson) {
    return new AssetJson(terminalJson.terminalId(), terminalJson.terminalName(), AssetType.TERMINAL);
  }

  public static AssetJson fromCachedInformation(Integer assetId, String assetName, AssetType assetType) {
    LOGGER.warn("Had to fallback to asset cache info for: id {}, name {}, type {}", assetId, assetName, assetType);
    return new AssetJson(assetId, assetName, assetType);
  }

  @Override
  public String getSelectionId() {
    return assetId.toString() + assetType.name();
  }

  @Override
  public String getSelectionText() {
    return assetName;
  }
}
