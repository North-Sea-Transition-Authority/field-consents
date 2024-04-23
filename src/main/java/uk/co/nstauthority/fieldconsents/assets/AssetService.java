package uk.co.nstauthority.fieldconsents.assets;

import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Service
public class AssetService {

  private final FieldService fieldService;
  private final TerminalService terminalService;

  @Autowired
  public AssetService(FieldService fieldService, TerminalService terminalService) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  @Deprecated
  public Optional<AssetJson> getAssetFromKey(String assetKey) {

    if (assetKey == null) {
      return Optional.empty();
    } else if (assetKey.endsWith(AssetType.FIELD.name())) {
      var fieldId = Integer.valueOf(assetKey.replace(AssetType.FIELD.name(), ""));
      FieldJson fieldJson = fieldService.findField(fieldId, "Field asset picked from search selector")
          .orElse(null);
      return Optional.ofNullable(fieldJson);
    } else if (assetKey.endsWith(AssetType.TERMINAL.name())) {
      var terminalId = Integer.valueOf(assetKey.replace(AssetType.TERMINAL.name(), ""));
      TerminalJson terminalJson = terminalService.findTerminal(terminalId, "Terminal asset picked from search selector")
          .orElse(null);
      return Optional.ofNullable(terminalJson);
    } else {
      throw new RuntimeException("Not a valid AssetKey: " + assetKey);
    }
  }

  @Deprecated
  public AssetJson getAsset(String assetKey) {
    return getAssetFromKey(assetKey)
        .orElseThrow(() -> new RuntimeException("Asset with key %s not found".formatted(assetKey)));
  }

  public Optional<? extends AssetJson> getAsset(AssetKey assetKey, String requestPurpose) {
    if (Objects.isNull(assetKey)) {
      return Optional.empty();
    }

    return switch (assetKey.assetType()) {
      case FIELD -> fieldService.findField(assetKey.assetId(), requestPurpose);
      case TERMINAL -> terminalService.findTerminal(assetKey.assetId(), requestPurpose);
    };
  }

}
