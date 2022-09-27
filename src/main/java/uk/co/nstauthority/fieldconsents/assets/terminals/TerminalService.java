package uk.co.nstauthority.fieldconsents.assets.terminals;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Terminal;

@Service
public class TerminalService {

  private final TerminalApi terminalApi;

  static final TerminalsProjectionRoot terminalsProjectionRoot =
      new TerminalsProjectionRoot().terminalName().terminalId();

  static final TerminalProjectionRoot terminalProjectionRoot =
      new TerminalProjectionRoot().terminalName().terminalId();

  @Autowired
  public TerminalService(TerminalApi terminalApi) {
    this.terminalApi = terminalApi;
  }

  public List<TerminalJson> searchTerminals(String terminalName, String requestPurpose) {
    return terminalApi.searchTerminals(terminalName,
            Boolean.TRUE,
            terminalsProjectionRoot,
            requestPurpose)
        .stream()
        .map(this::convertTerminalToTerminalJson)
        .toList();
  }

  public Optional<TerminalJson> getTerminal(Integer terminalId, String requestPurpose) {
    return terminalApi.findTerminalById(terminalId,
            terminalProjectionRoot,
            requestPurpose)
        .map(this::convertTerminalToTerminalJson);
  }

  public TerminalJson getTerminalOrError(Integer terminalId, String requestPurpose) {
    return getTerminal(terminalId, requestPurpose)
        .orElseThrow(() -> new EntityNotFoundException("Terminal not found for terminal id %s".formatted(terminalId)));
  }

  private TerminalJson convertTerminalToTerminalJson(Terminal terminal) {
    return new TerminalJson(terminal.getTerminalId(), terminal.getTerminalName());
  }

}
