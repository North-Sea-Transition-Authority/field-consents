package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamMemberService teamMemberService;

  @InjectMocks
  private TeamService teamService;

  @Test
  void getTeam_whenMatch_thenReturnTeam() {

    var team = TeamTestUtil.Builder().build();

    when(teamRepository.findByIdAndTeamType(team.getId(), team.getTeamType())).thenReturn(Optional.of(team));
    var result = teamService.getTeam(TeamId.valueOf(team.getId()), team.getTeamType());

    assertThat(result).contains(team);
    verify(teamRepository).findByIdAndTeamType(team.getId(), team.getTeamType());
  }

  @Test
  void getTeam_whenNoMatch_thenEmptyOptionalReturned() {

    var team = TeamTestUtil.Builder().build();

    when(teamRepository.findByIdAndTeamType(team.getId(), team.getTeamType())).thenReturn(Optional.empty());
    var result = teamService.getTeam(TeamId.valueOf(team.getId()), team.getTeamType());

    assertThat(result).isEmpty();
    verify(teamRepository).findByIdAndTeamType(team.getId(), team.getTeamType());
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserBelongsTo_whenUserIsNotMember_thenNoTeamsReturned(TeamType teamType) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of());

    var result = teamService.getTeamsOfTypeThatUserBelongsTo(user, teamType);

    assertThat(result).isEmpty();
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserBelongsTo_whenUserIsMember_thenTeamsReturned(TeamType teamType) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = new Team();

    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));

    var result = teamService.getTeamsOfTypeThatUserBelongsTo(user, teamType);

    assertThat(result).containsExactly(team);
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @Test
  void getUserAccessibleTeams_whenCanManageIndustryTeams_thenIndustryTeamsReturned() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var industryTeamManager = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    var userOwnTeam = TeamTestUtil.Builder().build();
    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(List.of(industryTeamManager));

    when(teamRepository.findAllByTeamTypeIn(List.of(TeamType.INDUSTRY)))
        .thenReturn(List.of(industryTeam));

    when(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()))
        .thenReturn(List.of(userOwnTeam));

    var result = teamService.getUserAccessibleTeams(user);

    assertThat(result)
        .containsExactly(industryTeam, userOwnTeam);
  }

  @Test
  void getUserAccessibleTeams_whenOnlyHasAccessToViewOwnTeams_thenOnlyPersonalTeams() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var industryTeamManager = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    var userOwnTeam = TeamTestUtil.Builder().build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(List.of(industryTeamManager));

    when(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()))
        .thenReturn(List.of(userOwnTeam));

    var result = teamService.getUserAccessibleTeams(user);

    assertThat(result)
        .containsExactly(userOwnTeam);

    verify(teamRepository, never()).findAllByTeamTypeIn(any());
  }

  @Test
  void getUserAccessibleTeams_whenHasAccessToViewIndustryTeams_andIsInIndustryTeam_thenVerifyDistinct() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var industryTeamManager = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    var userOwnTeam = TeamTestUtil.Builder().build();
    var industryTeamId = randomInteger();
    var industryTeam = TeamTestUtil.Builder()
        .withId(industryTeamId)
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var duplicateIndustryTeam = TeamTestUtil.Builder()
        .withId(industryTeamId)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user))
        .thenReturn(List.of(industryTeamManager));

    when(teamRepository.findAllByTeamTypeIn(List.of(TeamType.INDUSTRY)))
        .thenReturn(List.of(industryTeam));

    when(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()))
        .thenReturn(List.of(userOwnTeam, duplicateIndustryTeam));

    var result = teamService.getUserAccessibleTeams(user);

    assertThat(result)
        .containsExactly(industryTeam, userOwnTeam);
  }
}