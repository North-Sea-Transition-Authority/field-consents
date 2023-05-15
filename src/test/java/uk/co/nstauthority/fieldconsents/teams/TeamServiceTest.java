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
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private PermissionService permissionService;

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

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserHasPermissionFor_whenUserIsNotMember_thenNoTeamsReturned(TeamType teamType) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of());

    var result = teamService.getTeamsOfTypeThatUserHasPermissionFor(user, teamType, RolePermission.VIEW_PERMISSIONS);

    assertThat(result).isEmpty();
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserHasPermissionFor_whenUserIsMemberAndHasPermission_thenTeamsReturned(TeamType teamType) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = new Team();

    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));
    when(permissionService.hasPermissionForTeam(team, user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(true);

    var result = teamService.getTeamsOfTypeThatUserHasPermissionFor(user, teamType, RolePermission.VIEW_PERMISSIONS);

    assertThat(result).containsExactly(team);
    verify(teamRepository, times(1)).findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType);
  }

  @ParameterizedTest
  @EnumSource(value = TeamType.class)
  void getTeamsOfTypeThatUserHasPermissionFor_whenUserIsMemberAndDoesntHavePermission_thenNoTeamsReturned(TeamType teamType) {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = new Team();

    when(teamRepository.findAllTeamsOfTypeThatUserIsMemberOf(user.wuaId(), teamType)).thenReturn(List.of(team));
    when(permissionService.hasPermissionForTeam(team, user, RolePermission.VIEW_PERMISSIONS))
        .thenReturn(false);

    var result = teamService.getTeamsOfTypeThatUserHasPermissionFor(user, teamType, RolePermission.VIEW_PERMISSIONS);

    assertThat(result).isEmpty();
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

  @Test
  void canUserAccessMultipleTeams_oneTeam() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var industryTeamViewer = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withRole(IndustryTeamRole.VIEWER)
        .build();

    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(industryTeamViewer));

    when(teamRepository.findAllTeamsThatUserIsMemberOf(user.wuaId()))
        .thenReturn(List.of(industryTeam));

    var result = teamService.canUserAccessMultipleTeams(user);

    assertThat(result).isFalse();
  }

  @Test
  void canUserAccessMultipleTeams_manyTeams() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var industryTeamManager = TeamMemberTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER)
        .build();

    var industryTeam1 = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    var industryTeam2 = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    when(teamMemberService.getUserAsTeamMembers(user)).thenReturn(List.of(industryTeamManager));

    when(teamRepository.findAllByTeamTypeIn(any()))
        .thenReturn(List.of(industryTeam1, industryTeam2));

    var result = teamService.canUserAccessMultipleTeams(user);

    assertThat(result).isTrue();
  }

  @Test
  void createTeam_verifySave() {
    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();

    teamService.createTeam(industryTeam);

    verify(teamRepository, times(1)).save(industryTeam);
  }

  @Test
  void getTeamByOrganisationGroupId_teamExists() {

    var organisationGroupId = 10000;
    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .withOrganisationGroupId(organisationGroupId)
        .build();

    when(teamRepository.findByOrganisationGroupId(organisationGroupId))
        .thenReturn(Optional.of(industryTeam));

    assertThat(teamService.getTeamByOrganisationGroupId(organisationGroupId))
        .contains(industryTeam);
  }

  @Test
  void getTeamByOrganisationGroupId_teamDoesntExist() {

    var organisationGroupId = 10000;

    when(teamRepository.findByOrganisationGroupId(organisationGroupId))
        .thenReturn(Optional.empty());

    assertThat(teamService.getTeamByOrganisationGroupId(organisationGroupId))
        .isEmpty();
  }
}
