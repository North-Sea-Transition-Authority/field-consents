package uk.co.nstauthority.fieldconsents.assets.terminals;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithNoOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1WithOperator;
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
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.terminal.TerminalApi;
import uk.co.fivium.energyportalapi.generated.client.TerminalsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Terminal;
import uk.co.fivium.energyportalapi.generated.types.TerminalClassificationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class TerminalSearchServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final String REQUEST_PURPOSE = "Terminal search service test";

  @Mock
  private TerminalApi terminalApi;

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private TerminalSearchService terminalSearchService;

  private final RequestPurpose requestPurpose = new RequestPurpose(REQUEST_PURPOSE);

  @Test
  void searchTerminals_allTestTerminals() {
    when(terminalApi.searchTerminals(eq("T"), isNull(), any(TerminalsProjectionRoot.class),
        eq(requestPurpose)))
        .thenReturn(terminalList);

    assertThat(terminalSearchService.searchTerminals("T", REQUEST_PURPOSE))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal1Json, terminal3Json));
  }

  @Test
  void searchTerminals_includeInUseFieldIds() {
    var terminalList = List.of(
        Terminal.newBuilder()
            .terminalId(1)
            .terminalName("T1")
            .terminalActive(true)
            .terminalClassificationType(TerminalClassificationType.EDU)
            .build(),
        Terminal.newBuilder()
            .terminalId(2)
            .terminalName("T2")
            .terminalActive(true)
            .terminalClassificationType(TerminalClassificationType.EDU)
            .build(),
        Terminal.newBuilder()
            .terminalId(3)
            .terminalName("T3")
            .terminalActive(false)
            .terminalClassificationType(TerminalClassificationType.EDU)
            .build()
    );
    var terminalJsonList = terminalList.stream().map(TerminalJson::from).toList();

    when(applicationAssetService.getAllUniqueAssetIdsForAssetType(AssetType.TERMINAL)).thenReturn(Set.of(3));

    when(terminalApi.searchTerminals(eq("T"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalList);

    assertThat(terminalSearchService.searchTerminals("T", REQUEST_PURPOSE))
        .usingRecursiveComparison()
        .isEqualTo(terminalJsonList);
  }

  @Test
  void searchTerminals_singleTestTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal3));

    List<TerminalJson> singleTestTerminal = terminalSearchService.searchTerminals("T3", REQUEST_PURPOSE);
    assertThat(singleTestTerminal).hasSize(1);
    assertThat(singleTestTerminal.get(0)).usingRecursiveComparison()
        .isEqualTo(terminal3Json);
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenRegulator_allTestTerminals() {
    when(terminalApi.searchTerminals(eq("T"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalsWithOperatorList);

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(true);

    assertThat(terminalSearchService.searchTerminalsWithOperatorForUser("T", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal1JsonWithOperator, terminal2JsonWithOperator, terminal3JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenRegulator_singleTestTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal3WithOperator));

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(true);

    assertThat(terminalSearchService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal3JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenConsultee_allTestTerminals() {
    when(terminalApi.searchTerminals(eq("T"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalsWithOperatorList);

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, Set.of(Role.VIEWER, Role.ALLOCATOR, Role.RESPONDER)))
        .thenReturn(true);

    assertThat(terminalSearchService.searchTerminalsWithOperatorForUser("T", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal1JsonWithOperator, terminal2JsonWithOperator, terminal3JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenConsultee_singleTestTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal3WithOperator));

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.REGULATOR, RoleGroup.REGULATOR_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(false);

    when(teamQueryService.userHasAtLeastOneStaticRole(USER, TeamType.CONSULTEE, Set.of(Role.VIEWER, Role.ALLOCATOR, Role.RESPONDER)))
        .thenReturn(true);

    assertThat(terminalSearchService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal3JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenIndustryUser_twoTerminals() {
    when(terminalApi.searchTerminals(eq("T"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalsWithOperatorList);

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    assertThat(terminalSearchService.searchTerminalsWithOperatorForUser("T", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal1JsonWithOperator, terminal2JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenIndustryUser_singleTerminal() {
    when(terminalApi.searchTerminals(eq("T3"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(terminalsWithOperatorList);

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(terminalSearchService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(List.of(terminal2JsonWithOperator));
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenIndustryUser_noPermission() {
    when(terminalApi.searchTerminals(eq("T3"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal1WithOperator));

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(terminalSearchService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }

  @Test
  void searchTerminalsWithOperatorForUser_whenIndustryUser_noOperator() {
    when(terminalApi.searchTerminals(eq("T3"), isNull(), any(TerminalsProjectionRoot.class), eq(requestPurpose)))
        .thenReturn(List.of(terminal1WithNoOperator));

    when(organisationUnitPermissionService.getOperatorsUserHasRoleFor(USER, RoleGroup.INDUSTRY_VIEW_CASE_PROCESSING_ROLES))
        .thenReturn(List.of(orgUnit1Json));

    assertThat(terminalSearchService.searchTerminalsWithOperatorForUser("T3", REQUEST_PURPOSE, USER))
        .usingRecursiveComparison()
        .isEqualTo(Collections.emptyList());
  }
}
