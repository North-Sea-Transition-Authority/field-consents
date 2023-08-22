package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRoleService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class OpredTeamServiceTest {

  private static final Team TEAM = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).build();
  private static final TeamId TEAM_ID = TEAM.toTeamId();
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  
  @Mock
  private TeamService teamService;

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @Spy
  @InjectMocks
  private OpredTeamService opredTeamService;

  @Test
  void getTeam_whenMatch_thenReturnTeam() {
    when(teamService.getTeam(TEAM_ID, TeamType.OPRED)).thenReturn(Optional.of(TEAM));
    assertThat(opredTeamService.getTeam(TEAM_ID)).contains(TEAM);
    verify(teamService).getTeam(TEAM_ID, TEAM.getTeamType());
  }

  @Test
  void getTeam_whenNoMatch_thenEmptyOptional() {
    when(teamService.getTeam(TEAM_ID, TeamType.OPRED)).thenReturn(Optional.empty());

    assertThat(opredTeamService.getTeam(TEAM_ID)).isEmpty();
    verify(teamService).getTeam(TEAM_ID, TEAM.getTeamType());
  }

  @Test
  void isAccessManager_whenAccessManager_thenTrue() {
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(TEAM_ID, USER, Set.of(OpredTeamRole.ACCESS_MANAGER.name()))).thenReturn(true);
    assertTrue(opredTeamService.isAccessManager(TEAM_ID, USER));
  }

  @Test
  void isAccessManager_whenAccessManager_thenFalse() {
    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(TEAM_ID, USER, Set.of(OpredTeamRole.ACCESS_MANAGER.name()))).thenReturn(false);
    assertFalse(opredTeamService.isAccessManager(TEAM_ID, USER));
  }

  @Test
  void addUserTeamRoles_verifyRepositoryInteractions() {
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();
    var roles = Set.of(OpredTeamRole.ACCESS_MANAGER);

    opredTeamService.addUserTeamRoles(TEAM, userToAdd, roles);

    var rolesAsStrings = roles.stream().map(OpredTeamRole::name).collect(Collectors.toSet());

    verify(teamMemberRoleService).addUserTeamRoles(TEAM, userToAdd, rolesAsStrings);
  }

  @Test
  void getTeamsForUser() {
    when(teamService.getTeamsOfTypeThatUserBelongsTo(USER, TeamType.OPRED)).thenReturn(List.of(TEAM));
    assertThat(opredTeamService.getTeamsForUser(USER)).containsExactly(TEAM);
  }

  @Test
  void createTeam() {
    var orgGroupName = "org group TEAM name";
    var orgGroupId = 10000;

    var expectedTeam = TeamTestUtil.Builder()
        .withId(null)
        .withTeamType(TeamType.OPRED)
        .withDisplayName(orgGroupName)
        .withOrganisationGroupId(orgGroupId)
        .build();

    var TEAM = opredTeamService.createTeam(orgGroupName, orgGroupId);

    assertThat(TEAM).usingRecursiveComparison().isEqualTo(expectedTeam);
    
    verify(teamService).createTeam(TEAM);
  }

  @Test
  void getTeamByOrganisationGroupId_teamNotFound() {
    var orgGroupId = 10000;
    when(teamService.getTeamByOrganisationGroupId(orgGroupId)).thenReturn(Optional.empty());
    assertThat(opredTeamService.getTeamByOrganisationGroupId(orgGroupId)).isEmpty();
  }

  @Test
  void getTeamByOrganisationGroupId_teamFound() {
    var orgGroupId = 10000;
    var TEAM = TeamTestUtil.Builder().withTeamType(TeamType.OPRED).withOrganisationGroupId(orgGroupId).build();

    when(teamService.getTeamByOrganisationGroupId(orgGroupId)).thenReturn(Optional.of(TEAM));

    assertThat(opredTeamService.getTeamByOrganisationGroupId(orgGroupId)).contains(TEAM);
  }
}
