package uk.co.nstauthority.fieldconsents.assets.terminals;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TerminalServiceTest {

//  TerminalJson terminal1 = new TerminalJson(1, "F1");
//  TerminalJson terminal2 = new TerminalJson(2, "F2");
//  TerminalJson terminal3 = new TerminalJson(3, "F3");

  TerminalJson barrowRivers = new TerminalJson(73, "Barrow Rivers");
  TerminalJson flotta = new TerminalJson(76, "Flotta");
  TerminalJson cawdor = new TerminalJson(115, "Grotto");

  TerminalService terminalService;

  @BeforeEach
  void setup() {
    terminalService = new TerminalService();
    // TODO need to mock whatever calls TerminalService is using, but can't at the moment as it's dummy data
    //when(TerminalData.terminals).thenReturn(List.of(terminal1, terminal2, terminal3));
  }

  @Test
  void getAllTerminals_allTerminals() {
    List<TerminalJson> allTerminals = terminalService.getAllTerminals();
    assertThat(allTerminals).contains(barrowRivers, flotta, cawdor);
    // TODO change to containsExactly when I can mock correctly assertThat(allTerminals).containsExactly(terminal1, terminal2, terminal3);
  }

  @Test
  void getTerminal_terminalExists() {
    var terminalJsonOptional = terminalService.getTerminal(barrowRivers.terminalId());
    assertThat(terminalJsonOptional.get()).isEqualTo(barrowRivers);
  }

  @Test
  void getTerminal_terminalNotExists() {
    var terminalJsonOptional = terminalService.getTerminal(0);
    assertThat(terminalJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void getTerminalOrError_terminalExists() {
    var terminalJson = terminalService.getTerminalOrError(barrowRivers.terminalId());
    assertThat(terminalJson).isEqualTo(barrowRivers);
  }

  @Test
  void getTerminalOrError_terminalNotExists() {
    assertThatThrownBy(() -> terminalService.getTerminalOrError(0))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Terminal not found for terminal id 0");
  }

}
