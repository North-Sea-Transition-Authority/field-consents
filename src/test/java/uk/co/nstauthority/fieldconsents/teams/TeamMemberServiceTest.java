package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import java.util.List;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamMemberServiceTest {

  @Mock
  private TeamMemberRoleRepository teamMemberRoleRepository;

  @InjectMocks
  private TeamMemberService teamMemberService;

  @Test
  void getAllTeamMembers_whenMembers_thenNotEmpty() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var roleBuilder = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withWebUserAccountId(1L);

    var teamMemberRoleAccessManager = roleBuilder.withRole(RegulatorTeamRole.ACCESS_MANAGER.name())
        .build();

    var teamMemberRoleThirdPartyAccessManager = roleBuilder
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER.name())
        .build();

    when(teamMemberRoleRepository.findAllByTeam(team)).thenReturn(
        List.of(teamMemberRoleAccessManager, teamMemberRoleThirdPartyAccessManager));

    var result = teamMemberService.getTeamMembers(team);

    var teamView = TeamTestUtil.createTeamView(team);

    var expectedTeamMember = new TeamMember(new WebUserAccountId(1L), teamView, Set.of(RegulatorTeamRole.ACCESS_MANAGER,
        RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER));

    assertThat(result).containsExactly(expectedTeamMember);
  }

  @Test
  void getAllTeamMembers_whenNoMembers_thenEmpty() {
    var team = new Team();

    when(teamMemberRoleRepository.findAllByTeam(team)).thenReturn(List.of());

    var result = teamMemberService.getTeamMembers(team);

    assertThat(result).isEmpty();
    verify(teamMemberRoleRepository).findAllByTeam(team);
  }

  @Test
  void isMemberOfTeam_whenMember_thenTrue() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(randomInteger());

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_Id(user.wuaId(), teamId.id())).thenReturn(true);

    assertTrue(teamMemberService.isMemberOfTeam(teamId, user));
  }

  @Test
  void isMemberOfTeam_whenNotMember_thenFalse() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(randomInteger());

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_Id(user.wuaId(), teamId.id())).thenReturn(false);

    assertFalse(teamMemberService.isMemberOfTeam(teamId, user));
  }

  @Test
  void isMemberOfTeamWithAnyRoleOf_whenMemberWithRole_thenTrue() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(randomInteger());
    var roles = Set.of("ROLE_NAME");

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_IdAndRoleIn(user.wuaId(), teamId.id(), roles))
        .thenReturn(true);

    assertTrue(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, roles));
  }

  @Test
  void isMemberOfTeamWithAnyRoleOf_whenNotMemberWithRole_thenFalse() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var teamId = new TeamId(randomInteger());
    var roles = Set.of("ROLE_NAME");

    when(teamMemberRoleRepository.existsByWuaIdAndTeam_IdAndRoleIn(user.wuaId(), teamId.id(), roles))
        .thenReturn(false);

    teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, roles);

    assertFalse(teamMemberService.isMemberOfTeamWithAnyRoleOf(teamId, user, roles));
  }

  @Test
  void getTeamMembers_whenTeamMemberHasMultipleRoles_thenRolesMappedCorrectly() {

    var team = TeamTestUtil.Builder().build();

    var webUserAccountId = new WebUserAccountId(100);

    var firstRole = RegulatorTeamRole.ACCESS_MANAGER;
    var secondRole = RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER;

    team.setTeamType(TeamType.REGULATOR);

    var roleBuilder = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withWebUserAccountId(webUserAccountId.id());

    // GIVEN a user with multiple roles in the same team
    var firstTeamMemberRole = roleBuilder
        .withRole(firstRole.name())
        .build();

    var secondTeamMemberRole = roleBuilder
        .withRole(secondRole.name())
        .build();

    when(teamMemberRoleRepository.findAllByTeam(team)).thenReturn(
        List.of(firstTeamMemberRole, secondTeamMemberRole));

    // WHEN we get the team members for that team
    var resultingTeamMembers = teamMemberService.getTeamMembers(team);

    // THEN one team member is returned with the multiple roles
    assertThat(resultingTeamMembers)
        .extracting(TeamMember::wuaId, TeamMember::roles)
        .containsExactly(
            tuple(
                webUserAccountId,
                Set.of(secondRole, firstRole)
            )
        );
  }

  @Test
  void getTeamMember_whenMemberIsInTeam_thenGetTeamMember() {
    var team = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();

    var wuaId = new WebUserAccountId(123);
    var role = TeamMemberRoleTestUtil.Builder()
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER.name())
        .withWebUserAccountId(wuaId.id())
        .build();

    var teamView = TeamTestUtil.createTeamView(team);

    when(teamMemberRoleRepository.findAllByTeamAndWuaId(team, wuaId.id()))
        .thenReturn(List.of(role));

    var result = teamMemberService.getTeamMember(team, wuaId);

    assertTrue(result.isPresent());
    assertThat(result.get()).extracting(
        TeamMember::teamView,
        TeamMember::wuaId
    ).containsExactly(
        teamView,
        wuaId
    );

    assertThat(result.get().roles()).containsExactly(
        RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER
    );
  }

  @Test
  void getTeamMember_whenMemberIsNotInTeam_thenEmpty() {
    var team = new Team(randomInteger());
    team.setTeamType(TeamType.REGULATOR);

    var wuaId = new WebUserAccountId(123);

    when(teamMemberRoleRepository.findAllByTeamAndWuaId(team, wuaId.id())).thenReturn(List.of());

    var result = teamMemberService.getTeamMember(team, wuaId);

    assertTrue(result.isEmpty());
  }

  @Test
  void getUserAsTeamMembers_whenUserIsNotInTeam_thenNoResults() {

    var user = ServiceUserDetailTestUtil.Builder().build();
    when(teamMemberRoleRepository.findAllByWuaId(user.wuaId())).thenReturn(List.of());

    var result = teamMemberService.getUserAsTeamMembers(user);

    assertThat(result).isEmpty();
  }

  @Test
  void getUserAsTeamMembers_whenUserIsInTeam_thenCorrectlyMapped() {

    var user = ServiceUserDetailTestUtil.Builder().build();
    var team = TeamTestUtil.Builder().build();
    var teamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(team)
        .withRole(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER.name())
        .withWebUserAccountId(user.wuaId())
        .build();

    when(teamMemberRoleRepository.findAllByWuaId(user.wuaId())).thenReturn(List.of(teamMemberRole));

    var result = teamMemberService.getUserAsTeamMembers(user);

    assertThat(result).extracting(TeamMember::wuaId, TeamMember::roles, TeamMember::teamView)
        .containsExactly(
            Tuple.tuple(
                new WebUserAccountId(user.wuaId()),
                Set.of(RegulatorTeamRole.INDUSTRY_ACCESS_MANAGER),
                TeamTestUtil.createTeamView(team)
            )
        );
  }

  @Test
  void getUserAsTeamMembers_verifyTeamMemberRoleMapping() {
    var user = ServiceUserDetailTestUtil.Builder().build();
    var regulatorTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.REGULATOR)
        .build();
    var regulatorTeamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(regulatorTeam)
        .withRole(RegulatorTeamRole.ACCESS_MANAGER.name())
        .withWebUserAccountId(user.wuaId())
        .build();

    var industryTeam = TeamTestUtil.Builder()
        .withTeamType(TeamType.INDUSTRY)
        .build();
    var industryTeamMemberRole = TeamMemberRoleTestUtil.Builder()
        .withTeam(industryTeam)
        .withRole(IndustryTeamRole.ACCESS_MANAGER.name())
        .withWebUserAccountId(user.wuaId())
        .build();

    when(teamMemberRoleRepository.findAllByWuaId(user.wuaId()))
        .thenReturn(List.of(regulatorTeamMemberRole, industryTeamMemberRole));

    var result = teamMemberService.getUserAsTeamMembers(user);

    assertThat(result).extracting(TeamMember::roles)
        .containsExactlyInAnyOrder(
            Set.of(RegulatorTeamRole.ACCESS_MANAGER),
            Set.of(IndustryTeamRole.ACCESS_MANAGER)
        );
  }

}