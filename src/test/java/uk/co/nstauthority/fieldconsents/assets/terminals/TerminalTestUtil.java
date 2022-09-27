package uk.co.nstauthority.fieldconsents.assets.terminals;

import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Terminal;

public class TerminalTestUtil {
  public static Terminal terminal1 = Terminal.newBuilder().terminalId(1).terminalName("T1").build();
  public static TerminalJson terminal1Json = new TerminalJson(terminal1.getTerminalId(), terminal1.getTerminalName());
  public static Terminal terminal2 = Terminal.newBuilder().terminalId(2).terminalName("T2").build();
  public static TerminalJson terminal2Json = new TerminalJson(terminal2.getTerminalId(), terminal2.getTerminalName());
  public static Terminal terminal3 = Terminal.newBuilder().terminalId(3).terminalName("T3").build();
  public static TerminalJson terminal3Json = new TerminalJson(terminal3.getTerminalId(), terminal3.getTerminalName());
  public static List<Terminal> terminalList = List.of(terminal1, terminal2, terminal3);
}
