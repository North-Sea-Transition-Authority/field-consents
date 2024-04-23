package uk.co.nstauthority.fieldconsents.assets.terminals;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalWithOperatorProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;

@ExtendWith(MockitoExtension.class)
public class TerminalServiceTest {

  private static final String REQUEST_PURPOSE = "Terminal service test";

  @Mock
  private TerminalApi terminalApi;

  @InjectMocks
  private TerminalService terminalService;

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

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

  @Test
  void findTerminalsWithOperator_noIdsProvided() {
    assertThat(terminalService.findTerminalsWithOperator(Collections.emptyList(), "")).isEmpty();
    verifyNoInteractions(terminalApi);
  }

  @Test
  void findTerminalsWithOperator() {
    var ids = List.of(1, 2, 3, 4);
    var requestPurpose = new RequestPurpose("request purpose");

    when(terminalApi.findTerminalById(anyInt(), eq(terminalWithOperatorProjectionRoot), eq(requestPurpose))).thenReturn(Optional.of(terminal1WithOperator));

    assertThat(terminalService.findTerminalsWithOperator(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(terminal1JsonWithOperator, terminal1JsonWithOperator, terminal1JsonWithOperator, terminal1JsonWithOperator);

    // TODO: FCS-427 (remove n+1)
    verify(terminalApi).findTerminalById(1, terminalWithOperatorProjectionRoot, requestPurpose);
    verify(terminalApi).findTerminalById(2, terminalWithOperatorProjectionRoot, requestPurpose);
    verify(terminalApi).findTerminalById(3, terminalWithOperatorProjectionRoot, requestPurpose);
    verify(terminalApi).findTerminalById(4, terminalWithOperatorProjectionRoot, requestPurpose);
  }

  @Test
  void findTerminalsWithOperator_someIdsNotFound() {
    var ids = List.of(1, 2);
    var requestPurpose = new RequestPurpose("request purpose");

    when(terminalApi.findTerminalById(1, terminalWithOperatorProjectionRoot, requestPurpose)).thenReturn(Optional.of(terminal1WithOperator));
    when(terminalApi.findTerminalById(2, terminalWithOperatorProjectionRoot, requestPurpose)).thenReturn(Optional.empty());

    assertThat(terminalService.findTerminalsWithOperator(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(terminal1JsonWithOperator);

    // TODO: FCS-427 (remove n+1)
    verify(terminalApi).findTerminalById(1, terminalWithOperatorProjectionRoot, requestPurpose);
    verify(terminalApi).findTerminalById(2, terminalWithOperatorProjectionRoot, requestPurpose);
  }

  @Test
  void getTerminals() {
    var terminalIds = List.of(1, 2, 3);

    when(terminalApi.findTerminalById(1, terminalProjectionRoot, requestPurpose)).thenReturn(Optional.of(terminal1));
    when(terminalApi.findTerminalById(2, terminalProjectionRoot, requestPurpose)).thenReturn(Optional.of(terminal2));
    when(terminalApi.findTerminalById(3, terminalProjectionRoot, requestPurpose)).thenReturn(Optional.empty());

    assertThat(terminalService.getTerminals(terminalIds, requestPurpose.purpose()))
        .extracting(
            TerminalJson::getId,
            TerminalJson::getName
        )
        .containsExactly(
            tuple(terminal1.getTerminalId(), terminal1.getTerminalName()),
            tuple(terminal2.getTerminalId(), terminal2.getTerminalName())
        );
  }

  @Test
  void getTerminals_noIdsProvided() {
    assertThat(terminalService.getTerminals(Collections.emptyList(), requestPurpose.purpose())).isEmpty();
  }
}
