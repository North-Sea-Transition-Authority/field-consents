package uk.co.nstauthority.fieldconsents.assets.terminals;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.terminalWithOperatorProjectionRoot;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithNoOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3WithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminalList;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminalsWithOperatorList;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
public class TerminalServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private TerminalApi terminalApi;

  @Mock
  private TeamService teamService;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @InjectMocks
  private TerminalService terminalService;

  private static final String REQUEST_PURPOSE = "Terminal service test";

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

  private final Team regulatorTeam = TeamTestUtil.Builder().withTeamType(TeamType.REGULATOR).build();

  private final Team consulteeTeam = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();

  @Test
  void searchTerminals_allTestTerminals() {
    when(terminalApi.searchTerminals(eq("T"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalList);

    List<TerminalJson> allTestTerminals = terminalService.searchTerminals("T", REQUEST_PURPOSE);
    assertThat(allTestTerminals).hasSize(3);
    assertThat(allTestTerminals.get(0)).usingRecursiveComparison()
        .isEqualTo(terminal1Json);
    assertThat(allTestTerminals.get(1)).usingRecursiveComparison()
        .isEqualTo(terminal2Json);
    assertThat(allTestTerminals.get(2)).usingRecursiveComparison()
        .isEqualTo(terminal3Json);
  }

  @Test
  void searchTerminals_singleTestTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal3));

    List<TerminalJson> singleTestTerminal = terminalService.searchTerminals("T3", REQUEST_PURPOSE);
    assertThat(singleTestTerminal).hasSize(1);
    assertThat(singleTestTerminal.get(0)).usingRecursiveComparison()
        .isEqualTo(terminal3Json);
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenRegulator_allTestTerminals() {
    when(terminalApi.searchTerminals(eq("T"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalsWithOperatorList);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));

    assertThat(terminalService.searchTerminalsWithOperatorForUser("T", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal1JsonWithOperator, terminal2JsonWithOperator, terminal3JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenRegulator_singleTestTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal3WithOperator));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(regulatorTeam));

    assertThat(terminalService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal3JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenConsultee_allTestTerminals() {
    when(terminalApi.searchTerminals(eq("T"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalsWithOperatorList);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(consulteeTeam));

    assertThat(terminalService.searchTerminalsWithOperatorForUser("T", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal1JsonWithOperator, terminal2JsonWithOperator, terminal3JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenConsultee_singleTestTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal3WithOperator));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(consulteeTeam));

    assertThat(terminalService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal3JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenIndustryUser_twoTerminals() {
    when(terminalApi.searchTerminals(eq("T"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalsWithOperatorList);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    assertThat(terminalService.searchTerminalsWithOperatorForUser("T", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal1JsonWithOperator, terminal2JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenIndustryUser_singleTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalsWithOperatorList);

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(terminalService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal2JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenIndustryUser_noPermission() {
    when(terminalApi.searchTerminals(eq("T3"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal1WithOperator));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(terminalService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenIndustryUser_noOperator() {
    when(terminalApi.searchTerminals(eq("T3"), eq(Boolean.TRUE),
        any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal1WithNoOperator));

    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.REGULATOR, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());
    when(teamService.getTeamsOfTypeThatUserHasPermissionFor(USER, TeamType.OPRED, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(Collections.emptyList());

    when(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(List.of(orgUnit1Json));

    assertThat(terminalService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void findTerminal_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1));

    var terminalJsonOptional = terminalService.findTerminal(terminal1.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isPresent();
    assertThat(terminalJsonOptional.get()).usingRecursiveComparison()
        .isEqualTo(terminal1Json);
  }

  @Test
  void findTerminalWithOperator_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1WithOperator.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1WithOperator));

    var terminalJsonOptional =
        terminalService.findTerminalWithOperator(terminal1WithOperator.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isPresent();
    assertThat(terminalJsonOptional.get()).usingRecursiveComparison()
        .isEqualTo(terminal1JsonWithOperator);
  }

  @Test
  void findTerminal_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var terminalJsonOptional = terminalService.findTerminal(0, REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isEqualTo(Optional.empty());
  }

  @Test
  void findTerminalWithOperator_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    var terminalJsonOptional = terminalService.findTerminalWithOperator(0, REQUEST_PURPOSE);
    assertThat(terminalJsonOptional).isNotPresent();
  }

  @Test
  void getTerminal_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1));

    var terminalJson = terminalService.getTerminal(terminal1.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJson).usingRecursiveComparison()
        .isEqualTo(terminal1Json);
  }

  @Test
  void getTerminalWithOperator_terminalExists() {
    when(terminalApi.findTerminalById(eq(terminal1WithOperator.getTerminalId()),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.of(terminal1WithOperator));

    var terminalJson = terminalService.getTerminalWithOperator(terminal1WithOperator.getTerminalId(), REQUEST_PURPOSE);
    assertThat(terminalJson).usingRecursiveComparison()
        .isEqualTo(terminal1JsonWithOperator);
  }

  @Test
  void getTerminal_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> terminalService.getTerminal(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Terminal not found for terminal id 0");
  }

  @Test
  void getTerminalWithOperator_terminalNotExists() {
    when(terminalApi.findTerminalById(eq(0),
        any(TerminalProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> terminalService.getTerminalWithOperator(0, REQUEST_PURPOSE))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Terminal not found for terminal id 0");
  }

  @Test
  void findTerminalsWithOperator_noIdsProvided() {
    assertThat(terminalService.findTerminalsWithOperator(Collections.emptyList(), "")).isEmpty();
    verifyNoInteractions(terminalApi);
  }

  @Test
  void findTerminalsWithOperator() {
    var ids = List.of(1, 2, 3, 4);
    var requestPurpose = new RequestPurpose("request purpose");

    when(terminalApi.findTerminalById(anyInt(), eq(terminalWithOperatorProjectionRoot), eq(requestPurpose))).thenReturn(Optional.of(terminal1WithOperator));

    assertThat(terminalService.findTerminalsWithOperator(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(terminal1JsonWithOperator, terminal1JsonWithOperator, terminal1JsonWithOperator, terminal1JsonWithOperator);

    // TODO: FCS-427 (remove n+1)
    verify(terminalApi).findTerminalById(1, terminalWithOperatorProjectionRoot, requestPurpose);
    verify(terminalApi).findTerminalById(2, terminalWithOperatorProjectionRoot, requestPurpose);
    verify(terminalApi).findTerminalById(3, terminalWithOperatorProjectionRoot, requestPurpose);
    verify(terminalApi).findTerminalById(4, terminalWithOperatorProjectionRoot, requestPurpose);
  }

  @Test
  void findTerminalsWithOperator_someIdsNotFound() {
    var ids = List.of(1, 2);
    var requestPurpose = new RequestPurpose("request purpose");

    when(terminalApi.findTerminalById(1, terminalWithOperatorProjectionRoot, requestPurpose)).thenReturn(Optional.of(terminal1WithOperator));
    when(terminalApi.findTerminalById(2, terminalWithOperatorProjectionRoot, requestPurpose)).thenReturn(Optional.empty());

    assertThat(terminalService.findTerminalsWithOperator(ids, requestPurpose.purpose()))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactly(terminal1JsonWithOperator);

    // TODO: FCS-427 (remove n+1)
    verify(terminalApi).findTerminalById(1, terminalWithOperatorProjectionRoot, requestPurpose);
    verify(terminalApi).findTerminalById(2, terminalWithOperatorProjectionRoot, requestPurpose);
  }

  @Test
  void getTerminals() {
    var terminalIds = List.of(1, 2, 3);

    when(terminalApi.findTerminalById(1, terminalProjectionRoot, requestPurpose)).thenReturn(Optional.of(terminal1));
    when(terminalApi.findTerminalById(2, terminalProjectionRoot, requestPurpose)).thenReturn(Optional.of(terminal2));
    when(terminalApi.findTerminalById(3, terminalProjectionRoot, requestPurpose)).thenReturn(Optional.empty());

    assertThat(terminalService.getTerminals(terminalIds, requestPurpose.purpose()))
        .extracting(
            TerminalJson::getId,
            TerminalJson::getName
        )
        .containsExactly(
            tuple(terminal1.getTerminalId(), terminal1.getTerminalName()),
            tuple(terminal2.getTerminalId(), terminal2.getTerminalName())
        );
  }

  @Test
  void getTerminals_noIdsProvided() {
    assertThat(terminalService.getTerminals(Collections.emptyList(), requestPurpose.purpose())).isEmpty();
  }
}
