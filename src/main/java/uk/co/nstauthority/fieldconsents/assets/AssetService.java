package uk.co.nstauthority.fieldconsents.assets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class AssetService {

  private final FieldService fieldService;

  private final TerminalService terminalService;

  @Autowired
  public AssetService(FieldService fieldService, TerminalService terminalService) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  public List<AssetWithOperatorJson> searchAssetsForUser(String assetName, ServiceUserDetail user) {
    var searchFieldsStream = fieldService
        .searchFieldsWithOperatorForUser(assetName, "Assets search selector (search fields)", user)
        .stream();
    var searchTerminalsStream = terminalService
        .searchTerminalsWithOperatorForUser(assetName, "Assets search selector (search terminals)", user)
        .stream();

    return Stream.concat(searchFieldsStream, searchTerminalsStream)
        .sorted(Comparator.comparing(a -> a.getName().toLowerCase()))
        .toList();
  }

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

  public List<AssetJson> searchFields(String fieldName) {
    List<FieldJson> fieldJsonList = fieldService.searchFields(fieldName, "Assets search selector (search fields)")
        .stream()
        .sorted(Comparator.comparing(a -> a.getName().toLowerCase()))
        .toList();

    return new ArrayList<>(fieldJsonList);
  }

  public AssetJson getAsset(String assetKey) {
    return getAssetFromKey(assetKey)
        .orElseThrow(() -> new RuntimeException("Asset with key %s not found".formatted(assetKey))
        );
  }
}
