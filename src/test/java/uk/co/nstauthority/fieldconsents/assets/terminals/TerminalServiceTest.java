package uk.co.nstauthority.fieldconsents.assets.terminals;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithOperator;
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
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;

@ExtendWith(MockitoExtension.class)
public class TerminalServiceTest {

  TerminalService terminalService;

  @Mock
  TerminalApi terminalApi;

  private static final String REQUEST_PURPOSE = "Terminal service test";

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

  @BeforeEach
  void setup() {
    terminalService = new TerminalService(terminalApi);
  }

  @Test
  void searchTerminals_allTestTerminals() {
    when(terminalApi.searchTerminals(eq("T"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalList);

    List<TerminalJson> allTestTerminals = terminalService.searchTerminals("T", REQUEST_PURPOSE);
    assertThat(allTestTerminals).hasSize(3);
    assertThat(allTestTerminals.get(0)).usingRecursiveComparison()
        .isEqualTo(terminal1Json);
    assertThat(allTestTerminals.get(1)).usingRecursiveComparison()
        .isEqualTo(terminal2Json);
    assertThat(allTestTerminals.get(2)).usingRecursiveComparison()
        .isEqualTo(terminal3Json);
  }

  @Test
  void searchTerminals_singleTestTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal3));

    List<TerminalJson> singleTestTerminal = terminalService.searchTerminals("T3", REQUEST_PURPOSE);
    assertThat(singleTestTerminal).hasSize(1);
    assertThat(singleTestTerminal.get(0)).usingRecursiveComparison()
        .isEqualTo(terminal3Json);
  }

  @Test
  void findTerminal_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1));

    var terminalJsonOptional = terminalService.findTerminal(terminal1.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isPresent();
    assertThat(terminalJsonOptional.get()).usingRecursiveComparison()
        .isEqualTo(terminal1Json);
  }

  @Test
  void findTerminalWithOperator_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1WithOperator.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1WithOperator));

    var terminalJsonOptional =
        terminalService.findTerminalWithOperator(terminal1WithOperator.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isPresent();
    assertThat(terminalJsonOptional.get()).usingRecursiveComparison()
        .isEqualTo(terminal1JsonWithOperator);
  }

  @Test
  void findTerminal_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var terminalJsonOptional = terminalService.findTerminal(0, REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void findTerminalWithOperator_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var terminalJsonOptional = terminalService.findTerminalWithOperator(0, REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isNotPresent();
  }

  @Test
  void getTerminal_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1));

    var terminalJson = terminalService.getTerminal(terminal1.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJson).usingRecursiveComparison()
        .isEqualTo(terminal1Json);
  }

  @Test
  void getTerminalWithOperator_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1WithOperator.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1WithOperator));

    var terminalJson = terminalService.getTerminalWithOperator(terminal1WithOperator.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJson).usingRecursiveComparison()
        .isEqualTo(terminal1JsonWithOperator);
  }

  @Test
  void getTerminal_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> terminalService.getTerminal(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Terminal not found for terminal id 0");
  }

  @Test
  void getTerminalWithOperator_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> terminalService.getTerminalWithOperator(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Terminal not found for terminal id 0");
  }
}
