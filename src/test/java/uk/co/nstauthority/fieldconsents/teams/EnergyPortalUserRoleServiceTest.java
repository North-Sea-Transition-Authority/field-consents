package uk.co.nstauthority.fieldconsents.teams;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderUserRolesService;

@ExtendWith(MockitoExtension.class)
class EnergyPortalUserRoleServiceTest {

  @Mock
  private TeamRoleRepository teamRoleRepository;

  @Mock
  private EnergyPortalServiceProviderUserRolesService energyPortalServiceProviderUserRolesService;

  @InjectMocks
  private EnergyPortalUserRoleService energyPortalUserRoleService;

  @Test
  void publishAllUserTeamRolesMessage() {
    var team1 = TeamTestUtil.newBuilder().build();
    var team2 = TeamTestUtil.newBuilder().build();

    var wuaId1Team1TeamRole = TeamRoleTestUtil.newBuilder()
        .withWuaId(1L)
        .withTeam(team1)
        .withRole(Role.ACCESS_MANAGER)
        .build();

    var wuaId1Team2TeamRole = TeamRoleTestUtil.newBuilder()
        .withWuaId(1L)
        .withTeam(team2)
        .withRole(Role.VIEWER)
        .build();

    var wuaId2Team2TeamRole1 = TeamRoleTestUtil.newBuilder()
        .withWuaId(2L)
        .withTeam(team2)
        .withRole(Role.ACCESS_MANAGER)
        .build();

    var wuaId2Team2TeamRole2 = TeamRoleTestUtil.newBuilder()
        .withWuaId(2L)
        .withTeam(team2)
        .withRole(Role.VIEWER)
        .build();

    when(teamRoleRepository.findAll()).thenReturn(List.of(
        wuaId1Team1TeamRole,
        wuaId1Team2TeamRole,
        wuaId2Team2TeamRole1,
        wuaId2Team2TeamRole2
    ));

    energyPortalUserRoleService.publishAllUserTeamRolesMessage();

    verify(energyPortalServiceProviderUserRolesService).publishUsersRolesForTeam(
        1L,
        team1.getId().toString(),
        team1.getTeamType().name(),
        Set.of(Role.ACCESS_MANAGER.name())
    );

    verify(energyPortalServiceProviderUserRolesService).publishUsersRolesForTeam(
        1L,
        team2.getId().toString(),
        team2.getTeamType().name(),
        Set.of(Role.VIEWER.name())
    );

    verify(energyPortalServiceProviderUserRolesService).publishUsersRolesForTeam(
        2L,
        team2.getId().toString(),
        team2.getTeamType().name(),
        Set.of(Role.VIEWER.name(), Role.ACCESS_MANAGER.name())
    );
  }
}