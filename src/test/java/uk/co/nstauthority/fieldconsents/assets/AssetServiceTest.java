package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

  FieldJson field1 = new FieldJson(1, "F1");
  FieldJson field2 = new FieldJson(2, "F2");
  TerminalJson terminal1 = new TerminalJson(1, "T1");
  TerminalJson terminal2 = new TerminalJson(2, "T2");

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
  void getAllAssets_verifyListAndOrder() {
    when(fieldService.getAllFields()).thenReturn(List.of(field2, field1));
    when(terminalService.getAllTerminals()).thenReturn(List.of(terminal2, terminal1));

    List<AssetJson> allAssets = assetService.getAllAssets();

    assertThat(allAssets).containsExactly(
        AssetJson.from(field1),
        AssetJson.from(field2),
        AssetJson.from(terminal1),
        AssetJson.from(terminal2));

  }

  @Test
  void getAssetFromKey_nullAssetKey() {

    Optional<AssetJson> assetJson = assetService.getAssetFromKey(null);
    assertThat(assetJson).isEqualTo(Optional.empty());

  }

  @Test
  void getAssetFromKey_field() {
    when(fieldService.getField(field1.fieldId())).thenReturn(Optional.of(field1));

    Optional<AssetJson> assetJson = assetService.getAssetFromKey("1FIELD");
    assertThat(assetJson).isEqualTo(Optional.of(AssetJson.from(field1)));
  }

  @Test
  void getAssetFromKey_terminal() {
    when(terminalService.getTerminal(terminal1.terminalId())).thenReturn(Optional.of(terminal1));

    Optional<AssetJson> assetJson = assetService.getAssetFromKey("1TERMINAL");
    assertThat(assetJson).isEqualTo(Optional.of(AssetJson.from(terminal1)));
  }

  @Test
  void getAssetFromKey_invalidKey_thenException() {

    assertThatThrownBy(() -> assetService.getAssetFromKey("BADASSETKEY"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Not a valid AssetKey: BADASSETKEY");

  }

}
