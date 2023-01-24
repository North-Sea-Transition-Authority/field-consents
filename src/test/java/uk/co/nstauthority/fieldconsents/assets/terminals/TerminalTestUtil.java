package uk.co.nstauthority.fieldconsents.assets.terminals;

import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Terminal;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

public class TerminalTestUtil {

  public static final Integer TERMINAL_ID_1 = 1;
  public static final Integer TERMINAL_ID_2 = 2;
  public static final Integer TERMINAL_ID_3 = 3;

  public static final String TERMINAL_NAME_1 = "T1";
  public static final String TERMINAL_NAME_2 = "T2";
  public static final String TERMINAL_NAME_3 = "T3";

  public static Terminal terminal1 = Terminal.newBuilder().terminalId(TERMINAL_ID_1).terminalName(TERMINAL_NAME_1).build();
  public static Terminal terminal1WithOperator = Terminal.newBuilder().terminalId(TERMINAL_ID_1).terminalName(TERMINAL_NAME_1)
      .terminalOperator(OrganisationUnitTestUtil.orgUnit1).build();

  public static TerminalJson terminal1Json = new TerminalJson(
      terminal1.getTerminalId(),
      terminal1.getTerminalName()
  );

  public static TerminalWithOperatorJson terminal1JsonWithOperator = new TerminalWithOperatorJson(
      terminal1WithOperator.getTerminalId(),
      terminal1WithOperator.getTerminalName(),
      OrganisationUnitJson.from(terminal1WithOperator.getTerminalOperator())
  );

  public static Terminal terminal2 = Terminal.newBuilder().terminalId(TERMINAL_ID_2).terminalName(TERMINAL_NAME_2).build();

  public static TerminalJson terminal2Json = new TerminalJson(
      terminal2.getTerminalId(),
      terminal2.getTerminalName()
  );

  public static Terminal terminal3 = Terminal.newBuilder().terminalId(TERMINAL_ID_3).terminalName(TERMINAL_NAME_3).build();

  public static TerminalJson terminal3Json = new TerminalJson(
      terminal3.getTerminalId(),
      terminal3.getTerminalName()
  );
  public static List<Terminal> terminalList = List.of(terminal1, terminal2, terminal3);
}
