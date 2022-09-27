package uk.co.nstauthority.fieldconsents.assets;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
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

  public List<AssetJson> searchAssets(String assetName) {
    var searchFieldsStream = fieldService.searchFields(assetName, "Assets search selector (search fields)").stream()
        .map(AssetJson::from);
    var searchTerminalsStream = terminalService.searchTerminals(assetName, "Assets search selector (search terminals)").stream()
        .map(AssetJson::from);

    return Stream.concat(searchFieldsStream, searchTerminalsStream)
        .sorted(Comparator.comparing(a -> a.assetName().toLowerCase()))
        .toList();
  }

  public Optional<AssetJson> getAssetFromKey(String assetKey) {

    if (assetKey == null) {
      return Optional.empty();
    } else if (assetKey.endsWith(AssetType.FIELD.name())) {
      var fieldId = Integer.valueOf(assetKey.replace(AssetType.FIELD.name(), ""));
      return fieldService.getField(fieldId, "Field asset picked from search selector").map(AssetJson::from);
    } else if (assetKey.endsWith(AssetType.TERMINAL.name())) {
      var terminalId = Integer.valueOf(assetKey.replace(AssetType.TERMINAL.name(), ""));
      return terminalService.getTerminal(terminalId, "Terminal asset picked from search selector").map(AssetJson::from);
    } else {
      throw new RuntimeException("Not a valid AssetKey: " + assetKey);
    }

  }

}
