package uk.co.nstauthority.fieldconsents.assets.terminals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.fivium.energyportalapi.generated.types.Terminal;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

public class TerminalJson implements AssetJson {

  private final Integer terminalId;

  private final String terminalName;

  private static final Logger LOGGER = LoggerFactory.getLogger(TerminalJson.class);

  public static TerminalJson from(Terminal terminal) {
    return new TerminalJson(terminal.getTerminalId(), terminal.getTerminalName());
  }

  public static TerminalJson fromCachedInformation(Integer terminalId, String terminalName) {
    LOGGER.warn("Had to fallback to terminal cache info for: id {}, name {}", terminalId, terminalName);
    return new TerminalJson(terminalId, terminalName);
  }

  public TerminalJson(Integer terminalId, String terminalName) {
    this.terminalId = terminalId;
    this.terminalName = terminalName;
  }

  @Override
  public Integer getId() {
    return terminalId;
  }

  @Override
  public String getName() {
    return terminalName;
  }

  @Override
  public AssetType getAssetType() {
    return AssetType.TERMINAL;
  }
}
