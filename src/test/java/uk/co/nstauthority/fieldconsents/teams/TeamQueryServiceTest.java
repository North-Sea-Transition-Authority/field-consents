package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class TeamQueryServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamRoleRepository teamRoleRepository;

  @InjectMocks
  private TeamQueryService teamQueryService;

  private final ServiceUserDetail serviceUserDetail = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void userHasStaticRole_hasRole() {
    setupStaticTeamAndRoles(serviceUserDetail, TeamType.REGULATOR, List.of(
        Role.INDUSTRY_ACCESS_MANAGER,
        Role.ACCESS_MANAGER
    ));

    assertThat(teamQueryService.userHasStaticRole(serviceUserDetail, TeamType.REGULATOR, Role.INDUSTRY_ACCESS_MANAGER))
        .isTrue();
  }

  @Test
  void userHasStaticRole_doesNotHaveRole() {
    setupStaticTeamAndRoles(serviceUserDetail, TeamType.REGULATOR, List.of(
        Role.INDUSTRY_ACCESS_MANAGER,
        Role.ACCESS_MANAGER
    ));

    assertThat(teamQueryService.userHasStaticRole(serviceUserDetail, TeamType.REGULATOR, Role.VIEWER))
        .isFalse();
  }

  @Test
  void userHasStaticRole_invalidRole() {
    assertThat(teamQueryService.userHasStaticRole(serviceUserDetail, TeamType.REGULATOR, Role.EDITOR))
        .isFalse();
  }

  @Test
  void userHasStaticRole_noTeamInstance() {
    when(teamRepository.findByTeamType(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(teamQueryService.userHasStaticRole(serviceUserDetail, TeamType.REGULATOR, Role.VIEWER))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneStaticRole_hasRole() {
    setupStaticTeamAndRoles(serviceUserDetail, TeamType.REGULATOR, List.of(
        Role.INDUSTRY_ACCESS_MANAGER,
        Role.ACCESS_MANAGER
    ));

    assertThat(teamQueryService.userHasAtLeastOneStaticRole(serviceUserDetail, TeamType.REGULATOR, Set.of(Role.INDUSTRY_ACCESS_MANAGER, Role.VIEWER)))
        .isTrue();
  }

  @Test
  void userHasAtLeastOneStaticRole_doesNotHaveRole() {
    setupStaticTeamAndRoles(serviceUserDetail, TeamType.REGULATOR, List.of(
        Role.INDUSTRY_ACCESS_MANAGER,
        Role.ACCESS_MANAGER
    ));

    assertThat(teamQueryService.userHasAtLeastOneStaticRole(serviceUserDetail, TeamType.REGULATOR, Set.of(Role.VIEWER)))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneStaticRole_invalidRole() {
    assertThat(teamQueryService.userHasAtLeastOneStaticRole(serviceUserDetail, TeamType.REGULATOR, Set.of(Role.EDITOR)))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneStaticRole_noTeamInstance() {
    when(teamRepository.findByTeamType(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(teamQueryService.userHasAtLeastOneStaticRole(serviceUserDetail, TeamType.REGULATOR, Set.of(Role.INDUSTRY_ACCESS_MANAGER)))
        .isFalse();
  }

  @Test
  void userHasScopedRole_hasRole() {
    var scope = TeamScopeReference.from("123", TeamScopeReference.ORGANISATION_GROUP_ID);
    setupScopedTeamAndRoles(serviceUserDetail, TeamType.INDUSTRY, scope, List.of(
        Role.ACCESS_MANAGER,
        Role.VIEWER
    ));

    assertThat(teamQueryService.userHasScopedRole(serviceUserDetail, TeamType.INDUSTRY, scope, Role.VIEWER))
        .isTrue();
  }

  @Test
  void userHasScopedRole_doesNotHaveRole() {
    var scope = TeamScopeReference.from("123", TeamScopeReference.ORGANISATION_GROUP_ID);
    setupScopedTeamAndRoles(serviceUserDetail, TeamType.INDUSTRY, scope, List.of(
        Role.ACCESS_MANAGER,
        Role.VIEWER
    ));

    assertThat(teamQueryService.userHasScopedRole(serviceUserDetail, TeamType.INDUSTRY, scope, Role.EDITOR))
        .isFalse();
  }

  @Test
  void userHasScopedRole_invalidRole() {
    assertThat(teamQueryService.userHasScopedRole(
        serviceUserDetail,
        TeamType.INDUSTRY,
        TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID),
        Role.INDUSTRY_ACCESS_MANAGER
    )).isFalse();
  }

  @Test
  void userHasScopedRole_noTeamInstance() {
    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, "1")).thenReturn(Optional.empty());
    assertThat(teamQueryService.userHasScopedRole(serviceUserDetail, TeamType.INDUSTRY, TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID), Role.VIEWER))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneScopedRole_hasRole() {
    var scope = TeamScopeReference.from("123", TeamScopeReference.ORGANISATION_GROUP_ID);
    setupScopedTeamAndRoles(serviceUserDetail, TeamType.INDUSTRY, scope, List.of(
        Role.ACCESS_MANAGER,
        Role.VIEWER
    ));

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(serviceUserDetail, TeamType.INDUSTRY, scope, Set.of(Role.EDITOR, Role.VIEWER)))
        .isTrue();
  }

  @Test
  void userHasAtLeastOneScopedRole_doesNotHaveRole() {
    var scope = TeamScopeReference.from("123", TeamScopeReference.ORGANISATION_GROUP_ID);
    setupScopedTeamAndRoles(serviceUserDetail, TeamType.INDUSTRY, scope, List.of(
        Role.ACCESS_MANAGER,
        Role.VIEWER
    ));

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(serviceUserDetail, TeamType.INDUSTRY, scope, Set.of(Role.EDITOR)))
        .isFalse();
  }

  @Test
  void userHasAtLeastOneScopedRole_invalidRole() {
    assertThat(teamQueryService.userHasScopedRole(
        serviceUserDetail,
        TeamType.INDUSTRY,
        TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID),
        Role.CONSENTS_AND_AUTHORISATIONS_MANAGER
    )).isFalse();
  }

  @Test
  void userHasAtLeastOneScopedRole_noTeamInstance() {
    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, "1"))
        .thenReturn(Optional.empty());

    assertThat(teamQueryService.userHasAtLeastOneScopedRole(serviceUserDetail, TeamType.INDUSTRY, TeamScopeReference.from("1", TeamScopeReference.ORGANISATION_GROUP_ID), Set.of(Role.VIEWER)))
        .isFalse();
  }

  @Test
  void getStaticTeamRoles_nonStaticTeamTypeProvided() {
    var user = mock(ServiceUserDetail.class);
    assertThatThrownBy(() -> teamQueryService.getStaticTeamRoles(user, TeamType.INDUSTRY))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getStaticTeamRoles() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamRoles = List.of(
        mock(TeamRole.class),
        mock(TeamRole.class),
        mock(TeamRole.class)
    );

    when(teamRoleRepository.findAllByWuaIdAndTeam_TeamType(user.wuaId(), TeamType.REGULATOR))
        .thenReturn(teamRoles);

    assertThat(teamQueryService.getStaticTeamRoles(user, TeamType.REGULATOR))
        .isEqualTo(teamRoles);
  }

  @Test
  void getStaticTeam_nonStaticTeam() {
    assertThatThrownBy(() -> teamQueryService.getStaticTeam(TeamType.INDUSTRY))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getStaticTeam_moreThanOneTeamFound() {
    when(teamRepository.findByTeamType(TeamType.REGULATOR))
        .thenReturn(List.of(
            mock(Team.class),
            mock(Team.class)
        ));

    assertThatThrownBy(() -> teamQueryService.getStaticTeam(TeamType.REGULATOR))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getStaticTeam() {
    var team = mock(Team.class);

    when(teamRepository.findByTeamType(TeamType.REGULATOR))
        .thenReturn(List.of(team));

    assertThat(teamQueryService.getStaticTeam(TeamType.REGULATOR))
        .isEqualTo(team);
  }

  @Test
  void getIndustryTypeTeamByScopeId(){
    var team = mock(Team.class);

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, "1"))
        .thenReturn(Optional.of(team));

    assertThat(teamQueryService.getIndustryTypeTeamByScopeId("1"))
        .contains(team);
  }

  private void setupStaticTeamAndRoles(ServiceUserDetail userDetail, TeamType teamType, List<Role> roles) {
    var team = new Team(UUID.randomUUID());
    team.setTeamType(teamType);
    var teamRoles = roles.stream()
        .map(role -> createTeamRole(userDetail, team, role))
        .toList();

    when(teamRepository.findByTeamType(teamType))
        .thenReturn(List.of(team));
    when(teamRoleRepository.findByWuaIdAndTeam(userDetail.wuaId(), team))
        .thenReturn(teamRoles);
  }

  private void setupScopedTeamAndRoles(ServiceUserDetail userDetail, TeamType teamType, TeamScopeReference scopeRef, List<Role> roles) {
    var team = new Team(UUID.randomUUID());
    team.setScopeType(scopeRef.getType());
    team.setScopeId(scopeRef.getId());
    team.setTeamType(teamType);
    var teamRoles = roles.stream()
        .map(role -> createTeamRole(userDetail, team, role))
        .toList();

    when(teamRepository.findByTeamTypeAndScopeTypeAndScopeId(teamType, scopeRef.getType(), scopeRef.getId()))
        .thenReturn(Optional.of(team));
    when(teamRoleRepository.findByWuaIdAndTeam(userDetail.wuaId(), team))
        .thenReturn(teamRoles);
  }


  private TeamRole createTeamRole(ServiceUserDetail userDetail, Team team, Role role) {
    var teamRole = new TeamRole(UUID.randomUUID());
    teamRole.setWuaId(userDetail.wuaId());
    teamRole.setTeam(team);
    teamRole.setRole(role);
    return teamRole;
  }

}
