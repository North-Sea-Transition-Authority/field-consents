package uk.co.nstauthority.fieldconsents.assets;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class AssetService {

  private static final String SEARCH_FIELDS_PURPOSE = "Assets search selector (search fields)";
  private static final String SEARCH_TERMINALS_PURPOSE = "Assets search selector (search terminals)";

  private final FieldService fieldService;
  private final TerminalService terminalService;

  @Autowired
  public AssetService(FieldService fieldService, TerminalService terminalService) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  public List<AssetWithOperatorJson> searchAssetsForUser(String assetName, ServiceUserDetail user) {
    return consolidateAssets(
        fieldService.searchFieldsWithOperatorForUser(assetName, SEARCH_FIELDS_PURPOSE, user),
        terminalService.searchTerminalsWithOperatorForUser(assetName, SEARCH_TERMINALS_PURPOSE, user)
    );
  }

  public List<TerminalWithOperatorJson> searchTerminalsForUser(String terminalName, ServiceUserDetail user) {
    return terminalService.searchTerminalsWithOperatorForUser(terminalName, SEARCH_TERMINALS_PURPOSE, user);
  }

  public List<FieldWithOperatorJson> searchFieldsForUser(String fieldName, ServiceUserDetail user) {
    return fieldService.searchFieldsWithOperatorForUser(fieldName, SEARCH_FIELDS_PURPOSE, user);
  }

  public List<AssetJson> searchAssets(String assetName) {
    return consolidateAssets(
        fieldService.searchFields(assetName, SEARCH_FIELDS_PURPOSE),
        terminalService.searchTerminals(assetName, SEARCH_TERMINALS_PURPOSE)
    );
  }

  public List<AssetJson> searchTerminals(String assetName) {
    return consolidateAssets(Collections.emptyList(),
        terminalService.searchTerminals(assetName, SEARCH_TERMINALS_PURPOSE));
  }

  public List<AssetJson> searchFields(String fieldName) {
    return consolidateAssets(fieldService.searchFields(fieldName, SEARCH_FIELDS_PURPOSE), Collections.emptyList());
  }

  private <A extends AssetJson> List<A> consolidateAssets(List<? extends A> fields, List<? extends A> terminals) {
    return Stream
        .concat(fields.stream(), terminals.stream())
        .sorted(Comparator.comparing(a -> a.getName().toLowerCase()))
        .toList();
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
