package uk.co.nstauthority.fieldconsents.assets.terminals;

import uk.co.fivium.energyportalapi.generated.types.Terminal;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;

public class TerminalWithOperatorJson extends TerminalJson implements AssetWithOperatorJson {

  private final OrganisationUnitJson operatorJson;

  public static TerminalWithOperatorJson from(Terminal terminal) {
    return new TerminalWithOperatorJson(
        terminal.getTerminalId(),
        terminal.getTerminalName(),
        TerminalStatus.from(terminal),
        terminal.getTerminalOperator() != null
            ? OrganisationUnitJson.from(terminal.getTerminalOperator())
            : null
    );
  }

  public TerminalWithOperatorJson(Integer terminalId,
                                  String terminalName,
                                  TerminalStatus status,
                                  OrganisationUnitJson operatorJson) {
    super(terminalId, terminalName, status);
    this.operatorJson = operatorJson;
  }

  @Override
  public OrganisationUnitJson getOperatorJson() {
    return operatorJson;
  }
}
