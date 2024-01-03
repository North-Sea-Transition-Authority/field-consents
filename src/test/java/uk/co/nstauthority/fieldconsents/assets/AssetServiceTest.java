package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.BAD_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3JsonWithOperator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

  private static final String SEARCH_FIELDS_PURPOSE = "Assets search selector (search fields)";
  private static final String SEARCH_TERMINALS_PURPOSE = "Assets search selector (search terminals)";

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @InjectMocks
  private AssetService assetService;

  private final ServiceUserDetail user = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void searchAssetsForUser_verifyListAndOrder() {
    when(fieldService
        .searchFieldsWithOperatorForUser("1", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of(field1JsonWithOperator));
    when(terminalService
        .searchTerminalsWithOperatorForUser("1", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of(terminal1JsonWithOperator));

    var searchAssetsResults = assetService.searchAssetsForUser("1", user);

    assertThat(searchAssetsResults).containsExactly(
        field1JsonWithOperator,
        terminal1JsonWithOperator);
  }

  @Test
  void searchAssetsForUser_verifyListAndOrderFieldsOnly() {
    when(fieldService
        .searchFieldsWithOperatorForUser("F", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of(field3JsonWithOperator, field1JsonWithOperator, field2JsonWithOperator));
    when(terminalService
        .searchTerminalsWithOperatorForUser("F", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of());

    var searchAssetsResults = assetService.searchAssetsForUser("F", user);

    assertThat(searchAssetsResults).containsExactly(
        field1JsonWithOperator,
        field2JsonWithOperator,
        field3JsonWithOperator);
  }

  @Test
  void searchAssetsForUser_verifyListAndOrderTerminalsOnly() {
    when(fieldService
        .searchFieldsWithOperatorForUser("T", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of());
    when(terminalService
        .searchTerminalsWithOperatorForUser("T", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of(terminal2JsonWithOperator, terminal3JsonWithOperator, terminal1JsonWithOperator));

    var searchAssetsResults = assetService.searchAssetsForUser("T", user);

    assertThat(searchAssetsResults).containsExactly(
        terminal1JsonWithOperator,
        terminal2JsonWithOperator,
        terminal3JsonWithOperator);
  }

  @Test
  void searchFieldsForUser_whenNotFound() {
    when(fieldService
        .searchFieldsWithOperatorForUser("F", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of());

    var searchAssetsResults = assetService.searchFieldsForUser("F", user);

    assertThat(searchAssetsResults).isEmpty();
  }

  @Test
  void searchFieldsForUser_whenFound() {
    when(fieldService
        .searchFieldsWithOperatorForUser("F", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of(field1JsonWithOperator, field2JsonWithOperator, field3JsonWithOperator));

    var searchAssetsResults = assetService.searchFieldsForUser("F", user);

    assertThat(searchAssetsResults).containsExactly(
        field1JsonWithOperator,
        field2JsonWithOperator,
        field3JsonWithOperator);
  }

  @Test
  void searchTerminalsForUser_whenNotFound() {
    when(terminalService
        .searchTerminalsWithOperatorForUser("T", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of());

    var searchAssetsResults = assetService.searchTerminalsForUser("T", user);

    assertThat(searchAssetsResults).isEmpty();
  }

  @Test
  void searchTerminalsForUser_whenFound() {
    when(terminalService
        .searchTerminalsWithOperatorForUser("T", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of(terminal1JsonWithOperator, terminal2JsonWithOperator, terminal3JsonWithOperator));

    var searchAssetsResults = assetService.searchTerminalsForUser("T", user);

    assertThat(searchAssetsResults).containsExactly(
        terminal1JsonWithOperator,
        terminal2JsonWithOperator,
        terminal3JsonWithOperator);
  }

  @Test
  void searchAssets_verifyListAndOrder() {
    when(fieldService.searchFields("1", SEARCH_FIELDS_PURPOSE)).thenReturn(List.of(field1Json));
    when(terminalService.searchTerminals("1", SEARCH_TERMINALS_PURPOSE)).thenReturn(List.of(terminal1Json));

    assertThat(assetService.searchAssets("1")).containsExactly(field1Json, terminal1Json);
  }

  @Test
  void searchAssets_verifyListAndOrderFieldsOnly() {
    when(fieldService.searchFields("1", SEARCH_FIELDS_PURPOSE)).thenReturn(List.of(field1Json));
    when(terminalService.searchTerminals("1", SEARCH_TERMINALS_PURPOSE)).thenReturn(Collections.emptyList());

    assertThat(assetService.searchAssets("1")).containsExactly(field1Json);
  }

  @Test
  void searchAssets_verifyListAndOrderTerminalsOnly() {
    when(fieldService.searchFields("1", SEARCH_FIELDS_PURPOSE)).thenReturn(Collections.emptyList());
    when(terminalService.searchTerminals("1", SEARCH_TERMINALS_PURPOSE)).thenReturn(List.of(terminal1Json));

    assertThat(assetService.searchAssets("1")).containsExactly(terminal1Json);
  }

  @Test
  void searchFields_verifyListAndOrderFieldsOnly() {
    when(fieldService.searchFields("F", SEARCH_FIELDS_PURPOSE))
        .thenReturn(List.of(field1Json, field2Json, field3Json));

    List<AssetJson> searchAssetsResults = assetService.searchFields("F");

    assertThat(searchAssetsResults).containsExactly(
        field1Json,
        field2Json,
        field3Json
    );
  }

  @Test
  void getAssetFromKey_nullAssetKey() {

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(null);
    assertThat(assetJson).isEqualTo(Optional.empty());

  }

  @Test
  void getAssetFromKey_field() {
    when(fieldService.findField(field1Json.getId(), "Field asset picked from search selector"))
        .thenReturn(Optional.of(field1Json));

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(FIELD1_ASSET_KEY);
    assertThat(assetJson).isEqualTo(Optional.of(field1Json));
  }

  @Test
  void getAssetFromKey_terminal() {
    when(terminalService.findTerminal(terminal1Json.getId(), "Terminal asset picked from search selector"))
        .thenReturn(Optional.of(terminal1Json));

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(TERMINAL1_ASSET_KEY);
    assertThat(assetJson).isEqualTo(Optional.of(terminal1Json));
  }

  @Test
  void getAssetFromKey_invalidKey_thenException() {

    assertThatThrownBy(() -> assetService.getAssetFromKey(BAD_ASSET_KEY))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Not a valid AssetKey: " + BAD_ASSET_KEY);

  }

  @Test
  void getAssetOld_validKey() {
    when(fieldService.findField(FIELD_ID_1, "Field asset picked from search selector"))
        .thenReturn(Optional.of(field1Json));

    AssetJson assetJson = assetService.getAsset(FIELD1_ASSET_KEY);
    assertThat(assetJson).isEqualTo(field1Json);
  }

  @Test
  void getAssetOld_invalidKey_thenException() {

    assertThatThrownBy(() -> assetService.getAsset(BAD_ASSET_KEY))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Not a valid AssetKey: " + BAD_ASSET_KEY);

  }

  @Test
  void getAsset_field() {
    var assetKey = new AssetKey(1, AssetType.FIELD);
    var requestPurpose = "request purpose";

    when(fieldService.findField(assetKey.assetId(), requestPurpose)).thenReturn(Optional.of(field1Json));

    assertThat(assetService.getAsset(assetKey, requestPurpose))
        .isPresent()
        .get()
        .isEqualTo(field1Json);
  }

  @Test
  void getAsset_terminal() {
    var assetKey = new AssetKey(1, AssetType.TERMINAL);
    var requestPurpose = "request purpose";

    when(terminalService.findTerminal(assetKey.assetId(), requestPurpose)).thenReturn(Optional.of(terminal2JsonWithOperator));

    assertThat(assetService.getAsset(assetKey, requestPurpose))
        .isPresent()
        .get()
        .isEqualTo(terminal2JsonWithOperator);
  }

  @Test
  void getAsset_nullAssetKey() {
    assertThat(assetService.getAsset(null, "request purpose")).isEmpty();
  }

}
