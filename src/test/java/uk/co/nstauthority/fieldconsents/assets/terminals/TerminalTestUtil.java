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

  public static Terminal terminal1 = Terminal.newBuilder().terminalId(TERMINAL_ID_1).terminalName(TERMINAL_NAME_1)
      .terminalActive(Boolean.TRUE)
      .build();
  public static Terminal terminal1WithOperator = Terminal.newBuilder().terminalId(TERMINAL_ID_1).terminalName(TERMINAL_NAME_1)
      .terminalActive(Boolean.TRUE)
      .terminalOperator(OrganisationUnitTestUtil.orgUnit1).build();

  public static Terminal terminal1WithNoOperator = Terminal.newBuilder().terminalId(TERMINAL_ID_1).terminalName(TERMINAL_NAME_1)
      .terminalActive(Boolean.TRUE)
      .terminalOperator(null).build();

  public static TerminalJson terminal1Json = new TerminalJson(
      terminal1.getTerminalId(),
      terminal1.getTerminalName(),
      TerminalStatus.ACTIVE);

  public static TerminalWithOperatorJson terminal1JsonWithOperator = new TerminalWithOperatorJson(
      terminal1WithOperator.getTerminalId(),
      terminal1WithOperator.getTerminalName(),
      TerminalStatus.ACTIVE,
      OrganisationUnitJson.from(terminal1WithOperator.getTerminalOperator())
  );

  public static TerminalWithOperatorJson terminal1JsonWithNullOperator = new TerminalWithOperatorJson(
      terminal1.getTerminalId(),
      terminal1.getTerminalName(),
      TerminalStatus.ACTIVE,
      null
  );

  public static Terminal terminal2 = Terminal.newBuilder().terminalId(TERMINAL_ID_2).terminalName(TERMINAL_NAME_2)
      .terminalActive(Boolean.FALSE)
      .build();

  public static Terminal terminal2WithOperator = Terminal.newBuilder().terminalId(TERMINAL_ID_2).terminalName(TERMINAL_NAME_2)
      .terminalActive(Boolean.TRUE)
      .terminalOperator(OrganisationUnitTestUtil.orgUnit2).build();

  public static TerminalJson terminal2Json = new TerminalJson(
      terminal2.getTerminalId(),
      terminal2.getTerminalName(),
      TerminalStatus.INACTIVE);

  public static TerminalWithOperatorJson terminal2JsonWithOperator = new TerminalWithOperatorJson(
      terminal2WithOperator.getTerminalId(),
      terminal2WithOperator.getTerminalName(),
      TerminalStatus.ACTIVE,
      OrganisationUnitJson.from(terminal2WithOperator.getTerminalOperator())
  );


  public static Terminal terminal3 = Terminal.newBuilder().terminalId(TERMINAL_ID_3).terminalName(TERMINAL_NAME_3)
      .terminalActive(Boolean.TRUE)
      .build();

  public static Terminal terminal3WithOperator = Terminal.newBuilder().terminalId(TERMINAL_ID_3).terminalName(TERMINAL_NAME_3)
      .terminalActive(Boolean.TRUE)
      .terminalOperator(OrganisationUnitTestUtil.orgUnit3).build();

  public static TerminalJson terminal3Json = new TerminalJson(
      terminal3.getTerminalId(),
      terminal3.getTerminalName(),
      TerminalStatus.ACTIVE);

  public static TerminalWithOperatorJson terminal3JsonWithOperator = new TerminalWithOperatorJson(
      terminal3WithOperator.getTerminalId(),
      terminal3WithOperator.getTerminalName(),
      TerminalStatus.ACTIVE,
      OrganisationUnitJson.from(terminal3WithOperator.getTerminalOperator())
  );

  public static List<Terminal> terminalList = List.of(terminal1, terminal2, terminal3);

  public static List<Terminal> terminalsWithOperatorList =
      List.of(terminal1WithOperator, terminal2WithOperator, terminal3WithOperator);
}
