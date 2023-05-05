package uk.co.nstauthority.fieldconsents.organisations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_ID_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1With2GroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1WithGroupsJson;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit2WithGroupsJsonNoGroups;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ExtendWith(MockitoExtension.class)
class OrganisationUnitPermissionServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private TeamService teamService;

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Test
  void hasApplicationPermission_whenNoOrganisationGroups_thenFalse() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit2WithGroupsJsonNoGroups);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS))
    ).isFalse();
  }

  @Test
  void hasApplicationPermission_whenNoTeamForOrganisationGroup_thenFalse() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1WithGroupsJson);
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.empty());

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS))
    ).isFalse();
  }

  @Test
  void hasApplicationPermission_whenNoPermissionForTeam_thenFalse() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1WithGroupsJson);
    var team = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team));
    when(permissionService.hasPermissionForTeam(any(), any(), any()))
        .thenReturn(false);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS))
    ).isFalse();
  }


  @Test
  void hasApplicationPermission_whenPermissionForTeam_thenTrue() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1WithGroupsJson);
    var team = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team));
    when(permissionService.hasPermissionForTeam(any(), any(), any()))
        .thenReturn(true);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS))
    ).isTrue();
  }

  @Test
  void hasApplicationPermission_whenPermissionForSecondTeam_thenTrue() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team2));
    when(permissionService.hasPermissionForTeam(team1, USER, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermissionForTeam(team2, USER, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS)))
        .thenReturn(true);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS))
    ).isTrue();
  }

  @Test
  void hasApplicationPermission_whenPermissionForFirstTeam_thenTrue() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(permissionService.hasPermissionForTeam(team1, USER, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS)))
        .thenReturn(true);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS))
    ).isTrue();
  }

  @Test
  void hasApplicationPermission_whenNoPermissionForAnyTeam_thenFalse() {
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(orgUnit1With2GroupsJson);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_1))
        .thenReturn(Optional.of(team1));
    when(teamService.getTeamByOrganisationGroupId(ORG_GROUP_ID_2))
        .thenReturn(Optional.of(team2));
    when(permissionService.hasPermissionForTeam(team1, USER, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS)))
        .thenReturn(false);
    when(permissionService.hasPermissionForTeam(team2, USER, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS)))
        .thenReturn(false);

    assertThat(
        organisationUnitPermissionService
            .hasOperatorPermission(USER, PRIMARY_OPERATOR_OU_ID_1, Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS))
    ).isFalse();
  }

  @Test
  void getOperatorsUserHasPermissionsFor_whenNoOperators() {
    var requiredPermissions = Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.INDUSTRY))
        .thenReturn(List.of(team1, team2));
    when(permissionService.hasPermissionForTeam(team1, USER, requiredPermissions))
        .thenReturn(false);
    when(permissionService.hasPermissionForTeam(team2, USER, requiredPermissions))
        .thenReturn(false);
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(Collections.emptyList()))
        .thenReturn(Collections.emptyList());

    assertThat(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, requiredPermissions))
        .isEmpty();
  }

  @Test
  void getOperatorsUserHasPermissionsFor_whenOneOperator() {
    var requiredPermissions = Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.INDUSTRY))
        .thenReturn(List.of(team1, team2));
    when(permissionService.hasPermissionForTeam(team1, USER, requiredPermissions))
        .thenReturn(false);
    when(permissionService.hasPermissionForTeam(team2, USER, requiredPermissions))
        .thenReturn(true);
    when(organisationGroupQueryService.getOrganisationUnitsByOrganisationGroupIds(List.of(team2.getOrganisationGroupId())))
        .thenReturn(List.of(orgUnit2Json));

    assertThat(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, requiredPermissions))
        .isEqualTo(List.of(orgUnit2Json));
  }

  @Test
  void getOperatorsUserHasPermissionsFor_whenManyOperators() {
    var requiredPermissions = Set.of(RolePermission.SUBMIT_FCS_APPLICATIONS);
    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_1).build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(ORG_GROUP_ID_2).build();
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.INDUSTRY))
        .thenReturn(List.of(team1, team2));
    when(permissionService.hasPermissionForTeam(team1, USER, requiredPermissions))
        .thenReturn(true);
    when(permissionService.hasPermissionForTeam(team2, USER, requiredPermissions))
        .thenReturn(true);
    when(organisationGroupQueryService
        .getOrganisationUnitsByOrganisationGroupIds(List.of(team1.getOrganisationGroupId(), team2.getOrganisationGroupId())))
        .thenReturn(List.of(orgUnit1Json, orgUnit2Json));

    assertThat(organisationUnitPermissionService.getOperatorsUserHasPermissionsFor(USER, requiredPermissions))
        .isEqualTo(List.of(orgUnit1Json, orgUnit2Json));
  }
}
