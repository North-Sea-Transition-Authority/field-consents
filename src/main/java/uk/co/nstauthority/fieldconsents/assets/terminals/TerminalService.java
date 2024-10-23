package uk.co.nstauthority.fieldconsents.assets.terminals;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;

@Service
public class TerminalService {

  public static final String TERMINAL_INACTIVE_VALIDATION_MESSAGE
      = "is an inactive facility";

  static final TerminalsProjectionRoot terminalsProjectionRoot =
      new TerminalsProjectionRoot()
          .terminalId()
          .terminalName()
          .terminalActive();
  static final TerminalsProjectionRoot terminalsWithOperatorProjectionRoot =
      terminalsProjectionRoot
          .terminalOperator().organisationUnitId().name().root();

  static final TerminalProjectionRoot terminalProjectionRoot =
      new TerminalProjectionRoot()
          .terminalId()
          .terminalName()
          .terminalActive();

  static final TerminalProjectionRoot terminalWithOperatorProjectionRoot =
      terminalProjectionRoot
          .terminalOperator().organisationUnitId().name().root();

  private final TerminalApi terminalApi;

  TerminalService(TerminalApi terminalApi) {
    this.terminalApi = terminalApi;
  }

  public Optional<TerminalJson> findTerminal(Integer terminalId, String requestPurpose) {
    return terminalApi.findTerminalById(terminalId,
            terminalProjectionRoot,
            new RequestPurpose(requestPurpose))
        .map(TerminalJson::from);
  }

  public Optional<TerminalWithOperatorJson> findTerminalWithOperator(Integer terminalId, String requestPurpose) {
    return terminalApi.findTerminalById(terminalId, terminalWithOperatorProjectionRoot, new RequestPurpose(requestPurpose))
        .map(TerminalWithOperatorJson::from);
  }

  public TerminalJson getTerminal(Integer terminalId, String requestPurpose) {
    return findTerminal(terminalId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Terminal not found for terminal id %s".formatted(terminalId)));
  }

  // TODO: FCS-427 (remove n+1)
  public List<TerminalJson> getTerminals(List<Integer> terminalIds, String requestPurpose) {
    if (terminalIds.isEmpty()) {
      return Collections.emptyList();
    }

    return terminalIds
        .stream()
        .map(id -> terminalApi.findTerminalById(id, terminalProjectionRoot, new RequestPurpose(requestPurpose)))
        .flatMap(Optional::stream)
        .map(TerminalJson::from)
        .toList();
  }

  public TerminalWithOperatorJson getTerminalWithOperator(Integer terminalId, String requestPurpose) {
    return findTerminalWithOperator(terminalId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Terminal not found for terminal id %s".formatted(terminalId)));
  }

  // TODO: FCS-427 (remove n+1)
  public List<TerminalWithOperatorJson> findTerminalsWithOperator(List<Integer> terminalIds, String epaRequestPurpose) {
    if (terminalIds.isEmpty()) {
      return Collections.emptyList();
    }

    return terminalIds
        .stream()
        .map(id -> terminalApi.findTerminalById(id, terminalWithOperatorProjectionRoot, new RequestPurpose(epaRequestPurpose)))
        .flatMap(Optional::stream)
        .map(TerminalWithOperatorJson::from)
        .toList();
  }

}
