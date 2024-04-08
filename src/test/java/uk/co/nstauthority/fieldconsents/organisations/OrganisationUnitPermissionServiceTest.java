package uk.co.nstauthority.fieldconsents.organisations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1With2GroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1WithGroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2WithGroupsJsonNoGroups;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class OrganisationUnitPermissionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private TeamService teamService;

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  @Spy
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void hasOperatorPermission_withVarargs(boolean hasOperatorPermission) {
    doReturn(hasOperatorPermission)
        .when(organisationUnitPermissionService)
        .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS));

    assertThat(organisationUnitPermissionService.hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS))
        .isEqualTo(hasOperatorPermission);
  }

  @Test
  void hasOperatorPermission_withSet_whenNoOrganisationGroups_thenFalse() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit2WithGroupsJsonNoGroups);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS))
    ).isFalse();
  }

  @Test
  void hasOperatorPermission_withSet_whenNoTeamForOrganisationGroup_thenFalse() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1WithGroupsJson);
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.empty());

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS))
    ).isFalse();
  }

  @Test
  void hasOperatorPermission_withSet_whenNoPermissionForTeam_thenFalse() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1WithGroupsJson);
    var team = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team));
    when(permissionService.hasPermissionForTeam(any(), any(), any()))
        .thenReturn(false);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS))
    ).isFalse();
  }


  @Test
  void hasOperatorPermission_withSet_whenPermissionForTeam_thenTrue() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1WithGroupsJson);
    var team = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team));
    when(permissionService.hasPermissionForTeam(any(), any(), any()))
        .thenReturn(true);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS))
    ).isTrue();
  }

  @Test
  void hasOperatorPermission_withSet_whenPermissionForSecondTeam_thenTrue() {
    var requiredPermissions = Set.of(RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team2));
    when(permissionService.hasPermissionForTeam(team1, USER, requiredPermissions))
        .thenReturn(false);
    when(permissionService.hasPermissionForTeam(team2, USER, requiredPermissions))
        .thenReturn(true);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, requiredPermissions)
    ).isTrue();
  }

  @Test
  void hasOperatorPermission_withSet_whenPermissionForFirstTeam_thenTrue() {
    var requiredPermissions = Set.of(RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(permissionService.hasPermissionForTeam(team1, USER, requiredPermissions))
        .thenReturn(true);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, requiredPermissions)
    ).isTrue();
  }

  @Test
  void hasOperatorPermission_withSet_whenNoPermissionForAnyTeam_thenFalse() {
    var requiredPermissions = Set.of(RolePermission.PAY_AND_SUBMIT_FCS_APPLICATIONS);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team2));
    when(permissionService.hasPermissionForTeam(team1, USER, requiredPermissions))
        .thenReturn(false);
    when(permissionService.hasPermissionForTeam(team2, USER, requiredPermissions))
        .thenReturn(false);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, requiredPermissions)
    ).isFalse();
  }

  @Test
  void getUserPermissionsForOperator_whenNoOrganisationGroups_thenEmpty() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit2WithGroupsJsonNoGroups);

    assertThat(organisationUnitPermissionService.getUserPermissionsForOperator(USER, PRIMARY_OPERATOR_OU_ID_1))
        .isEmpty();
  }

  @Test
  void getUserPermissionsForOperator_whenNoTeamForOrganisationGroup_thenEmpty() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1WithGroupsJson);
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.empty());

    assertThat(organisationUnitPermissionService.getUserPermissionsForOperator(USER, PRIMARY_OPERATOR_OU_ID_1))
        .isEmpty();
  }

  @Test
  void getUserPermissionsForOperator_whenNoPermissionForAnyTeam_thenEmpty() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team2));
    when(permissionService.getUserPermissionsForTeam(team1, USER))
        .thenReturn(Collections.emptySet());
    when(permissionService.getUserPermissionsForTeam(team2, USER))
        .thenReturn(Collections.emptySet());

    assertThat(organisationUnitPermissionService.getUserPermissionsForOperator(USER, PRIMARY_OPERATOR_OU_ID_1))
        .isEmpty();
  }

  @Test
  void getUserPermissionsForOperator_whenPermissionForFirstTeamOnly_thenPermissionsReturned() {
    var expectedPermissions = Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.VIEW_FCS_APPLICATIONS);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team2));
    when(permissionService.getUserPermissionsForTeam(team1, USER))
        .thenReturn(expectedPermissions);
    when(permissionService.getUserPermissionsForTeam(team2, USER))
        .thenReturn(Collections.emptySet());

    assertThat(organisationUnitPermissionService.getUserPermissionsForOperator(USER, PRIMARY_OPERATOR_OU_ID_1))
        .containsAll(expectedPermissions);
  }

  @Test
  void getUserPermissionsForOperator_whenPermissionForSecondTeamOnly_thenPermissionsReturned() {
    var expectedPermissions = Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.VIEW_FCS_APPLICATIONS);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team2));
    when(permissionService.getUserPermissionsForTeam(team1, USER))
        .thenReturn(Collections.emptySet());
    when(permissionService.getUserPermissionsForTeam(team2, USER))
        .thenReturn(expectedPermissions);

    assertThat(organisationUnitPermissionService.getUserPermissionsForOperator(USER, PRIMARY_OPERATOR_OU_ID_1))
        .containsAll(expectedPermissions);
  }

  @Test
  void getUserPermissionsForOperator_whenPermissionForAllTeams_thenPermissionsReturned() {
    var team1ExpectedPermissions = Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.VIEW_FCS_APPLICATIONS);
    var team2ExpectedPermissions = Set.of(RolePermission.PROCESS_FCS_APPLICATIONS, RolePermission.VIEW_FCS_CONSENTS,
        RolePermission.GRANT_ROLES);
    var expectedPermissions = Stream.of(team1ExpectedPermissions, team2ExpectedPermissions)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team2));
    when(permissionService.getUserPermissionsForTeam(team1, USER))
        .thenReturn(team1ExpectedPermissions);
    when(permissionService.getUserPermissionsForTeam(team2, USER))
        .thenReturn(team2ExpectedPermissions);

    assertThat(organisationUnitPermissionService.getUserPermissionsForOperator(USER, PRIMARY_OPERATOR_OU_ID_1))
        .containsAll(expectedPermissions);
  }
}
