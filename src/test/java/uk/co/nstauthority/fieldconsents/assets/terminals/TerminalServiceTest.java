package uk.co.nstauthority.fieldconsents.assets.terminals;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminalList;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;

@ExtendWith(MockitoExtension.class)
public class TerminalServiceTest {

  TerminalService terminalService;

  @Mock
  TerminalApi terminalApi;

  @BeforeEach
  void setup() {
    terminalService = new TerminalService(terminalApi);
  }

  @Test
  void searchTerminals_allTestTerminals() {
    when(terminalApi.searchTerminals(eq("T"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq("Search test terminals")))
        .thenReturn(terminalList);

    List<TerminalJson> allTestTerminals = terminalService.searchTerminals("T", "Search test terminals");
    assertThat(allTestTerminals).containsExactly(terminal1Json, terminal2Json, terminal3Json);
  }

  @Test
  void searchTerminals_singleTestTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq("Search test terminals")))
        .thenReturn(List.of(terminal3));

    List<TerminalJson> singleTestTerminal = terminalService.searchTerminals("T3", "Search test terminals");
    assertThat(singleTestTerminal).containsExactly(terminal3Json);
  }

  @Test
  void getTerminal_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1.getTerminalId()),
        any(TerminalProjectionRoot.class), eq("Terminal service test")))
        .thenReturn(Optional.of(terminal1));

    var terminalJsonOptional = terminalService.getTerminal(terminal1.getTerminalId(), "Terminal service test");
    assertThat(terminalJsonOptional.get()).isEqualTo(terminal1Json);
  }

  @Test
  void getTerminal_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq("Terminal service test")))
        .thenReturn(Optional.empty());

    var terminalJsonOptional = terminalService.getTerminal(0, "Terminal service test");
    assertThat(terminalJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void getTerminalOrError_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1.getTerminalId()),
        any(TerminalProjectionRoot.class), eq("Terminal service test")))
        .thenReturn(Optional.of(terminal1));

    var terminalJson = terminalService.getTerminalOrError(terminal1.getTerminalId(), "Terminal service test");
    assertThat(terminalJson).isEqualTo(terminal1Json);
  }

  @Test
  void getTerminalOrError_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq("Terminal service test")))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> terminalService.getTerminalOrError(0, "Terminal service test"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Terminal not found for terminal id 0");
  }

}
