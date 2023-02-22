package uk.co.nstauthority.fieldconsents.assets.terminals;


import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

class TerminalJsonTest {

  @Test
  void from() {
    TerminalJson terminalJson = TerminalJson.from(terminal1);
    assertThat(terminalJson)
        .usingRecursiveComparison()
        .isEqualTo(
            new TerminalJson(
                terminal1.getTerminalId(),
                terminal1.getTerminalName(),
                Boolean.TRUE.equals(terminal1.getTerminalActive()) ? TerminalStatus.ACTIVE : TerminalStatus.INACTIVE
            )
        );

    // the asserts below check the getters in FieldJson
    assertThat(terminalJson)
        .extracting(
            TerminalJson::getId,
            TerminalJson::getName,
            TerminalJson::getStatusDisplayName,
            TerminalJson::getAssetType
        )
        .containsExactly(
            terminal1.getTerminalId(),
            terminal1.getTerminalName(),
            TerminalStatus.from(terminal1).getDisplayName(),
            AssetType.TERMINAL
        );
  }

  @Test
  void fromCachedInformation() {
    TerminalJson terminalJson =
        TerminalJson.fromCachedInformation(terminal1.getTerminalId(), terminal1.getTerminalName());
    assertThat(terminalJson)
        .usingRecursiveComparison()
        .isEqualTo(
            new TerminalJson(
                terminal1.getTerminalId(),
                terminal1.getTerminalName(),
                null
            )
        );

    // the asserts below check the getters in FieldJson
    assertThat(terminalJson)
        .extracting(
            TerminalJson::getId,
            TerminalJson::getName,
            TerminalJson::getStatusDisplayName,
            TerminalJson::getAssetType
        )
        .containsExactly(
            terminal1.getTerminalId(),
            terminal1.getTerminalName(),
            null,
            AssetType.TERMINAL
        );
  }
}