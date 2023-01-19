package uk.co.nstauthority.fieldconsents.assets.terminals;

import javax.validation.constraints.NotNull;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public record TerminalJson(
    @NotNull Integer terminalId,
    @NotNull String terminalName,
    Integer operatorOuId,
    String operatorName
) implements SearchSelectable {

  @Override
  public String getSelectionId() {
    return terminalId.toString();
  }

  @Override
  public String getSelectionText() {
    return terminalName;
  }

}
