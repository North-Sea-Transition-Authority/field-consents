package uk.co.nstauthority.fieldconsents.assets.terminals;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TerminalService {

  public List<TerminalJson> getAllTerminals() {
    return TerminalData.terminals;
  }

  public Optional<TerminalJson> getTerminal(Integer terminalId) {
    return TerminalData.terminals.stream().filter(fieldJson -> fieldJson.terminalId().equals(terminalId)).findFirst();
  }

  public TerminalJson getTerminalOrError(Integer terminalId) {
    return getTerminal(terminalId)
        .orElseThrow(() -> new RuntimeException("Terminal not found for terminal id %s".formatted(terminalId)));
  }

}
