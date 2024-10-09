package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.co.nstauthority.fieldconsents.assets.AssetType.FIELD;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AssetKeyTest {

  @Test
  void from_withString_valid() {
    assertThat(AssetKey.from("1FIELD")).isEqualTo(new AssetKey(1, FIELD));
  }

  @Test
  void from_withString_invalid() {
    assertThatThrownBy(() -> AssetKey.from("1"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid assetKey [1]");
  }

  @ParameterizedTest
  @MethodSource("getAssetIdsForTypeParams")
  void parse(String assetKeyString, AssetKey assetKey) {
    assertThat(AssetKey.parse(assetKeyString)).isEqualTo(Optional.ofNullable(assetKey));
  }

  private static Stream<Arguments> getAssetIdsForTypeParams() {
    return Stream.of(
        arguments("1FIELD", new AssetKey(1, FIELD)),
        arguments("2FIELD", new AssetKey(2, FIELD)),
        arguments("1TERMINAL", new AssetKey(1, AssetType.TERMINAL)),
        arguments("2TERMINAL", new AssetKey(2, AssetType.TERMINAL)),
        arguments("", null),
        arguments("1", null),
        arguments("INVALID", null),
        arguments("1INVALID", null),
        arguments("1TERMINALX", null),
        arguments("1TERMINAL ", null),
        arguments(" 1TERMINAL", null),
        arguments(" 1TERMINAL ", null),
        arguments("1TERMINALL", null),
        arguments("1TERMINALTERMINAL", null),
        arguments("1TERMINALFIELD", null),
        arguments("TERMINAL", null),
        arguments("FIELD", null),
        arguments("FIELD ", null),
        arguments(" FIELD", null),
        arguments("1FIELD1TERMINAL", null)
    );
  }

}
