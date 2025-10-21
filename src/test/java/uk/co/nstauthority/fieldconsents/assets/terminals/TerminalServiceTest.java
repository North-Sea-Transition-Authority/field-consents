package uk.co.nstauthority.fieldconsents.assets.terminals;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalsWithOperatorProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithNotEduClassification;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithOperatorAndNotEduClassification;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2WithNotEduClassification;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2WithOperatorAndNotEduClassification;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3WithOperator;

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
class TerminalServiceTest {

  private static final String REQUEST_PURPOSE = "Terminal service test";

  @Mock
  private TerminalApi terminalApi;

  @InjectMocks
  private TerminalService terminalService;

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

  @Test
  void findTerminal_terminalExists_correctTerminalType() {
    when(terminalApi.findTerminalById(eq(terminal1.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1));

    var terminalJsonOptional = terminalService.findTerminal(terminal1.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isPresent();
    assertThat(terminalJsonOptional.get()).usingRecursiveComparison()
        .isEqualTo(terminal1Json);
  }

  @Test
  void findTerminal_terminalExists_wrongTerminalType() {
    when(terminalApi.findTerminalById(
        eq(terminal1WithNotEduClassification.getTerminalId()),
        any(TerminalProjectionRoot.class),
        eq(requestPurpose)
    )).thenReturn(Optional.of(terminal1WithNotEduClassification));

    var terminalJsonOptional = terminalService
        .findTerminal(terminal1WithNotEduClassification.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isEmpty();
  }

  @Test
  void findTerminalWithOperator_terminalExists_correctTerminalType() {
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
  void findTerminalWithOperator_terminalExists_wrongTerminalType() {
    when(terminalApi.findTerminalById(eq(terminal1WithOperatorAndNotEduClassification.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1WithOperatorAndNotEduClassification));

    var terminalJsonOptional = terminalService
            .findTerminalWithOperator(terminal1WithOperatorAndNotEduClassification.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isEmpty();
  }

  @Test
  void findTerminal_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var terminalJsonOptional = terminalService.findTerminal(0, REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isNotPresent();
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
  void findTerminalsWithOperator_correctTerminalType() {
    var ids = List.of(1, 2, 3, 4);

    when(terminalApi.getTerminalsByIds(ids, terminalsWithOperatorProjectionRoot, requestPurpose))
        .thenReturn(List.of(terminal1WithOperator, terminal2WithOperator, terminal3WithOperator));

    assertThat(terminalService.findTerminalsWithOperator(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(terminal1JsonWithOperator, terminal2JsonWithOperator, terminal3JsonWithOperator);

    //No n+1
    verify(terminalApi, never())
        .findTerminalById(anyInt(), any(TerminalProjectionRoot.class), any(RequestPurpose.class));
  }

  @Test
  void findTerminalsWithOperator_wrongTerminalType() {
    var ids = List.of(1, 2, 3, 4);

    when(terminalApi.getTerminalsByIds(ids, terminalsWithOperatorProjectionRoot, requestPurpose))
        .thenReturn(List.of(terminal1WithOperatorAndNotEduClassification, terminal2WithOperatorAndNotEduClassification,
            terminal3WithOperator));

    assertThat(terminalService.findTerminalsWithOperator(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(terminal3JsonWithOperator);

    //No n+1
    verify(terminalApi, never())
        .findTerminalById(anyInt(), any(TerminalProjectionRoot.class), any(RequestPurpose.class));
  }

  @Test
  void findTerminalsWithOperator_someIdsNotFound() {
    var ids = List.of(1, 2);

    when(terminalApi.getTerminalsByIds(ids, terminalsWithOperatorProjectionRoot, requestPurpose))
        .thenReturn(List.of(terminal1WithOperator));

    assertThat(terminalService.findTerminalsWithOperator(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(terminal1JsonWithOperator);

    //No n+1
    verify(terminalApi, never())
        .findTerminalById(anyInt(), any(TerminalProjectionRoot.class), any(RequestPurpose.class));
  }

  @Test
  void getTerminals_correctTerminalType() {
    var terminalIds = List.of(1, 2, 3);

    when(terminalApi.getTerminalsByIds(terminalIds, terminalsWithOperatorProjectionRoot, requestPurpose))
        .thenReturn(List.of(terminal1, terminal2));

    assertThat(terminalService.getTerminals(terminalIds, requestPurpose.purpose()))
        .extracting(
            TerminalJson::getId,
            TerminalJson::getName
        )
        .containsExactly(
            tuple(terminal1.getTerminalId(), terminal1.getTerminalName()),
            tuple(terminal2.getTerminalId(), terminal2.getTerminalName())
        );

    //No n+1
    verify(terminalApi, never())
        .findTerminalById(anyInt(), any(TerminalProjectionRoot.class), any(RequestPurpose.class));
  }

  @Test
  void getTerminals_wrongTerminalType() {
    var terminalIds = List.of(1, 2, 3);

    when(terminalApi.getTerminalsByIds(terminalIds, terminalsWithOperatorProjectionRoot, requestPurpose))
        .thenReturn(List.of(terminal1WithNotEduClassification, terminal2WithNotEduClassification));

    assertThat(terminalService.getTerminals(terminalIds, requestPurpose.purpose())).isEqualTo(List.of());

    //No n+1
    verify(terminalApi, never())
        .findTerminalById(anyInt(), any(TerminalProjectionRoot.class), any(RequestPurpose.class));
  }

  @Test
  void getTerminals_noIdsProvided() {
    assertThat(terminalService.getTerminals(Collections.emptyList(), requestPurpose.purpose())).isEmpty();
  }
}
