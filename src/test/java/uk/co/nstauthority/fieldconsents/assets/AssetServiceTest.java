package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.BAD_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3Json;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

  @Mock
  FieldService fieldService;

  @Mock
  TerminalService terminalService;

  AssetService assetService;

  @BeforeEach
  void setup() {
    assetService = new AssetService(fieldService, terminalService);
  }

  @Test
  void searchAssets_verifyListAndOrder() {
    when(fieldService.searchFields("1", "Assets search selector (search fields)")).thenReturn(List.of(field1Json));
    when(terminalService.searchTerminals("1", "Assets search selector (search terminals)")).thenReturn(List.of(terminal1Json));

    List<AssetJson> searchAssetsResults = assetService.searchAssets("1");

    assertThat(searchAssetsResults).containsExactly(
        AssetJson.from(field1Json),
        AssetJson.from(terminal1Json));
  }

  @Test
  void searchAssets_verifyListAndOrderFieldsOnly() {
    when(fieldService.searchFields("F", "Assets search selector (search fields)")).thenReturn(List.of(field3Json, field1Json, field2Json));
    when(terminalService.searchTerminals("F", "Assets search selector (search terminals)")).thenReturn(List.of());

    List<AssetJson> searchAssetsResults = assetService.searchAssets("F");

    assertThat(searchAssetsResults).containsExactly(
        AssetJson.from(field1Json),
        AssetJson.from(field2Json),
        AssetJson.from(field3Json));
  }

  @Test
  void searchAssets_verifyListAndOrderTerminalsOnly() {
    when(fieldService.searchFields("T", "Assets search selector (search fields)")).thenReturn(List.of());
    when(terminalService.searchTerminals("T", "Assets search selector (search terminals)")).thenReturn(List.of(terminal2Json, terminal3Json, terminal1Json));

    List<AssetJson> searchAssetsResults = assetService.searchAssets("T");

    assertThat(searchAssetsResults).containsExactly(
        AssetJson.from(terminal1Json),
        AssetJson.from(terminal2Json),
        AssetJson.from(terminal3Json));
  }

  @Test
  void getAssetFromKey_nullAssetKey() {

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(null);
    assertThat(assetJson).isEqualTo(Optional.empty());

  }

  @Test
  void getAssetFromKey_field() {
    when(fieldService.getField(field1Json.fieldId(), "Field asset picked from search selector")).thenReturn(Optional.of(
        field1Json));

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(FIELD1_ASSET_KEY);
    assertThat(assetJson).isEqualTo(Optional.of(AssetJson.from(field1Json)));
  }

  @Test
  void getAssetFromKey_terminal() {
    when(terminalService.getTerminal(terminal1Json.terminalId(), "Terminal asset picked from search selector")).thenReturn(Optional.of(
        terminal1Json));

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(TERMINAL1_ASSET_KEY);
    assertThat(assetJson).isEqualTo(Optional.of(AssetJson.from(terminal1Json)));
  }

  @Test
  void getAssetFromKey_invalidKey_thenException() {

    assertThatThrownBy(() -> assetService.getAssetFromKey(BAD_ASSET_KEY))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Not a valid AssetKey: " + BAD_ASSET_KEY);

  }

}
