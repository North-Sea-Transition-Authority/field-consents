package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.BAD_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.FIELD1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.TERMINAL1_ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @InjectMocks
  private AssetService assetService;

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
