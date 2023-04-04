package uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberRoleService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class IndustryTeamServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private TeamMemberRoleService teamMemberRoleService;

  @InjectMocks
  @SpyBean
  private IndustryTeamService industryTeamService;

  @Test
  void getTeam_whenMatch_thenReturnTeam() {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    when(teamService.getTeam(teamId, TeamType.INDUSTRY)).thenReturn(Optional.of(team));

    assertThat(industryTeamService.getTeam(teamId)).contains(team);
    verify(teamService, times(1)).getTeam(teamId, team.getTeamType());
  }

  @Test
  void getTeam_whenNoMatch_thenEmptyOptional() {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var teamId = team.toTeamId();

    when(teamService.getTeam(teamId, TeamType.INDUSTRY)).thenReturn(Optional.empty());

    assertThat(industryTeamService.getTeam(teamId)).isEmpty();
    verify(teamService, times(1)).getTeam(teamId, team.getTeamType());
  }

  @Test
  void isAccessManager_whenAccessManager_thenTrue() {

    var teamId = new TeamId(randomInteger());
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(IndustryTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(true);

    assertTrue(industryTeamService.isAccessManager(teamId, user));
  }

  @Test
  void isAccessManager_whenAccessManager_thenFalse() {

    var teamId = new TeamId(randomInteger());
    var user = ServiceUserDetailTestUtil.Builder().build();

    when(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, Set.of(IndustryTeamRole.ACCESS_MANAGER.name())))
        .thenReturn(false);

    assertFalse(industryTeamService.isAccessManager(teamId, user));
  }

  @Test
  void addUserTeamRoles_verifyRepositoryInteractions() {

    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var userToAdd = EnergyPortalUserDtoTestUtil.Builder().build();
    var industryRoles = Set.of(
        IndustryTeamRole.ACCESS_MANAGER
    );

    industryTeamService.addUserTeamRoles(team, userToAdd, industryRoles);

    var rolesAsStrings = industryRoles
        .stream()
        .map(IndustryTeamRole::name)
        .collect(Collectors.toSet());

    verify(teamMemberRoleService, times(1)).addUserTeamRoles(team, userToAdd, rolesAsStrings);
  }

  @Test
  void getTeamsForUser() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().build();

    when(teamService.getTeamsOfTypeThatUserBelongsTo(user, TeamType.INDUSTRY))
        .thenReturn(List.of(team));

    var result = industryTeamService.getTeamsForUser(user);

    assertThat(result)
        .containsExactly(team);
  }

}
