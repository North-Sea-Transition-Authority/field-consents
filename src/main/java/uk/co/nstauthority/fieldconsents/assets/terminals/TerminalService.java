package uk.co.nstauthority.fieldconsents.assets.terminals;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;

@Service
public class TerminalService {

  private final TerminalApi terminalApi;

  static final TerminalsProjectionRoot terminalsProjectionRoot =
      new TerminalsProjectionRoot().terminalName().terminalId();

  static final TerminalProjectionRoot terminalProjectionRoot =
      new TerminalProjectionRoot().terminalName().terminalId();

  static final TerminalProjectionRoot terminalWithOperatorProjectionRoot =
      new TerminalProjectionRoot().terminalName().terminalId().terminalOperator().organisationUnitId().name().root();
  
  @Autowired
  public TerminalService(TerminalApi terminalApi) {
    this.terminalApi = terminalApi;
  }

  public List<TerminalJson> searchTerminals(String terminalName, String requestPurpose) {
    return terminalApi.searchTerminals(terminalName,
            Boolean.TRUE,
            terminalsProjectionRoot,
            new RequestPurpose(requestPurpose))
        .stream()
        .map(TerminalJson::from)
        .toList();
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

  public TerminalWithOperatorJson getTerminalWithOperator(Integer terminalId, String requestPurpose) {
    return findTerminalWithOperator(terminalId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Terminal not found for terminal id %s".formatted(terminalId)));
  }
}
