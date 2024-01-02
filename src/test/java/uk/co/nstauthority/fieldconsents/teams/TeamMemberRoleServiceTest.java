package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.teams.TeamTestUtil.randomInteger;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.EnergyPortalAccessService;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.InstigatingWebUserAccountId;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.ResourceType;
import uk.co.fivium.digital.energyportalteamaccesslibrary.team.TargetWebUserAccountId;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;

@ExtendWith(MockitoExtension.class)
class TeamMemberRoleServiceTest {

  @Mock
  private TeamMemberRoleRepository teamMemberRoleRepository;

  @Mock
  private EnergyPortalAccessService energyPortalAccessService;

  @Mock
  private UserDetailService userDetailService;

  @Captor
  private ArgumentCaptor<List<TeamMemberRole>> teamMemberRoleCaptor;

  @InjectMocks
  private TeamMemberRoleService teamMemberRoleService;

  @ParameterizedTest
  @EnumSource
  void addUserTeamRoles_whenAddingUser_andUserIsNew_thenVerifyCalls(TeamType teamType) {
    var team = TeamTestUtil.Builder().withTeamType(teamType).build();

    var userToAdd = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(100L)
        .build();

    var role = "ROLE_NAME";

    var instigatingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(200L)
        .build();

    when(userDetailService.getUserDetail()).thenReturn(instigatingUser);

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, Set.of(role));

    verify(teamMemberRoleRepository, times(1)).deleteAllByTeamAndWuaId(team, userToAdd.webUserAccountId());
    verify(teamMemberRoleRepository, times(1)).saveAll(teamMemberRoleCaptor.capture());

    assertThat(teamMemberRoleCaptor.getValue())
        .extracting(TeamMemberRole::getTeam, TeamMemberRole::getWuaId, TeamMemberRole::getRole)
        .containsExactly(tuple(team, userToAdd.webUserAccountId(), role));

    var targetWebUserAccountIdCaptor = ArgumentCaptor.forClass(TargetWebUserAccountId.class);
    var instigatingWebUserAccountIdCaptor = ArgumentCaptor.forClass(InstigatingWebUserAccountId.class);

    verify(energyPortalAccessService).addUserToAccessTeam(
        eq(new ResourceType(TeamMemberRoleService.RESOURCE_TYPE_NAME)),
        targetWebUserAccountIdCaptor.capture(),
        instigatingWebUserAccountIdCaptor.capture()
    );

    assertThat(targetWebUserAccountIdCaptor.getValue().getId())
        .isEqualTo(userToAdd.webUserAccountId());

    assertThat(instigatingWebUserAccountIdCaptor.getValue().getId())
        .isEqualTo(instigatingUser.wuaId());
  }

  @ParameterizedTest
  @EnumSource
  void addUserTeamRoles_whenAddingUser_andUserExists_thenVerifyCalls(TeamType teamType) {
    var team = TeamTestUtil.Builder().withTeamType(teamType).build();

    var userToAdd = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(100L)
        .build();

    var role = "ROLE_NAME";

    when(teamMemberRoleRepository.findAllByWuaId(userToAdd.webUserAccountId()))
        .thenReturn(List.of(new TeamMemberRole(anyInt())));

    teamMemberRoleService.addUserTeamRoles(team, userToAdd, Set.of(role));

    verify(teamMemberRoleRepository, times(1)).deleteAllByTeamAndWuaId(team, userToAdd.webUserAccountId());
    verify(teamMemberRoleRepository, times(1)).saveAll(teamMemberRoleCaptor.capture());

    assertThat(teamMemberRoleCaptor.getValue())
        .extracting(TeamMemberRole::getTeam, TeamMemberRole::getWuaId, TeamMemberRole::getRole)
        .containsExactly(tuple(team, userToAdd.webUserAccountId(), role));

    verify(energyPortalAccessService, never()).addUserToAccessTeam(any(), any(), any());
  }

  @Test
  void updateUserTeamRoles_whenMemberWithOneRole_thenVerifySingleRowInsert() {
    var team = TeamTestUtil.Builder().build();
    var existingUser = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100)
        .build();

    var role = "ROLE_NAME";

    teamMemberRoleService.updateUserTeamRoles(team, existingUser.wuaId(), Set.of(role));

    verify(teamMemberRoleRepository, times(1)).deleteAllByTeamAndWuaId(team, existingUser.wuaId().id());
    verify(teamMemberRoleRepository, times(1)).saveAll(teamMemberRoleCaptor.capture());

    assertThat(teamMemberRoleCaptor.getValue())
        .extracting(TeamMemberRole::getTeam, TeamMemberRole::getWuaId, TeamMemberRole::getRole)
        .containsExactly(tuple(team, existingUser.wuaId().id(), role));
  }

  @Test
  void updateUserTeamRoles_whenMemberWithMultipleRoles_thenVerifyMultipleRowInsert() {

    var team = TeamTestUtil.Builder().build();
    var existingUser = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(100)
        .build();

    var firstRole = "FIRST_ROLE_NAME";
    var secondRole = "SECOND_ROLE_NAME";

    var rolesToGrant = Set.of(firstRole, secondRole);

    teamMemberRoleService.updateUserTeamRoles(team, existingUser.wuaId(), rolesToGrant);

    verify(teamMemberRoleRepository, times(1)).deleteAllByTeamAndWuaId(team, existingUser.wuaId().id());
    verify(teamMemberRoleRepository, times(1)).saveAll(teamMemberRoleCaptor.capture());

    assertThat(teamMemberRoleCaptor.getValue())
        .extracting(TeamMemberRole::getTeam, TeamMemberRole::getWuaId, TeamMemberRole::getRole)
        .containsExactlyInAnyOrder(
            tuple(team, existingUser.wuaId().id(), firstRole),
            tuple(team, existingUser.wuaId().id(), secondRole)
        );

    verify(teamMemberRoleRepository).deleteAllByTeamAndWuaId(team, existingUser.wuaId().id());
  }

  @ParameterizedTest
  @EnumSource
  void removeMemberFromTeam_whenUserExistsInNoTeams_verifyInteractions(TeamType teamType) {

    var team = TeamTestUtil.Builder().withTeamType(teamType).build();
    var teamMember = TeamMemberTestUtil.Builder().build();

    var instigatingUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(200L)
        .build();

    when(userDetailService.getUserDetail()).thenReturn(instigatingUser);
    when(teamMemberRoleRepository.findAllByWuaId(teamMember.wuaId().id()))
        .thenReturn(List.of());

    teamMemberRoleService.removeMemberFromTeam(team, teamMember);

    verify(teamMemberRoleRepository).deleteAllByTeamAndWuaId(team, teamMember.wuaId().id());

    var targetWebUserAccountIdCaptor = ArgumentCaptor.forClass(TargetWebUserAccountId.class);
    var instigatingWebUserAccountIdCaptor = ArgumentCaptor.forClass(InstigatingWebUserAccountId.class);

    verify(energyPortalAccessService, times(1)).removeUserFromAccessTeam(
        eq(new ResourceType(TeamMemberRoleService.RESOURCE_TYPE_NAME)),
        targetWebUserAccountIdCaptor.capture(),
        instigatingWebUserAccountIdCaptor.capture()
    );

    assertThat(targetWebUserAccountIdCaptor.getValue().getId()).isEqualTo(teamMember.wuaId().id());
    assertThat(instigatingWebUserAccountIdCaptor.getValue().getId()).isEqualTo(instigatingUser.wuaId());
  }

  @ParameterizedTest
  @EnumSource
  void removeMemberFromTeam_whenUserStillExistsInTeams_noCallToFox_verifyInteractions(TeamType teamType) {

    var team = TeamTestUtil.Builder().withTeamType(teamType).build();
    var teamMember = TeamMemberTestUtil.Builder().build();

    when(teamMemberRoleRepository.findAllByWuaId(teamMember.wuaId().id()))
        .thenReturn(List.of(new TeamMemberRole(randomInteger())));

    teamMemberRoleService.removeMemberFromTeam(team, teamMember);

    verify(teamMemberRoleRepository).deleteAllByTeamAndWuaId(team, teamMember.wuaId().id());
    verify(energyPortalAccessService, never()).removeUserFromAccessTeam(any(), any(), any());
  }
}
