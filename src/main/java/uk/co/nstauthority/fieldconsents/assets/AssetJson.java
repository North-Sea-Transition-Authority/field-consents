package uk.co.nstauthority.fieldconsents.assets;

import javax.validation.constraints.NotNull;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public record AssetJson(
    @NotNull Integer assetId,
    @NotNull String assetName,
    @NotNull AssetType assetType
) implements SearchSelectable {

  public static AssetJson from(FieldJson fieldJson) {
    return new AssetJson(fieldJson.fieldId(), fieldJson.fieldName(), AssetType.FIELD);
  }

  public static AssetJson from(TerminalJson terminalJson) {
    return new AssetJson(terminalJson.terminalId(), terminalJson.terminalName(), AssetType.TERMINAL);
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
