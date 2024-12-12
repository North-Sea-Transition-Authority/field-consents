package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.management.TeamManagementException;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@ExtendWith(MockitoExtension.class)
class TeamMemberViewQueryServiceTest {

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private TeamMemberViewQueryService teamMemberViewQueryService;

  @Test
  void getTeamMemberViews_multipleUsers() {
    var teamA = TeamTestUtil.newBuilder().build();
    var teamB = TeamTestUtil.newBuilder().build();
    var teamC = TeamTestUtil.newBuilder().build();

    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withWuaId(1L)
            .withTeam(teamA)
            .withRole(Role.CASE_MANAGER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(1L)
            .withTeam(teamA)
            .withRole(Role.CASE_OFFICER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(1L)
            .withTeam(teamA)
            .withRole(Role.ACCESS_MANAGER)
            .build(),

        TeamRoleTestUtil.newBuilder()
            .withWuaId(2L)
            .withTeam(teamB)
            .withRole(Role.CONSENT_RECIPIENT)
            .build(),

        TeamRoleTestUtil.newBuilder()
            .withWuaId(3L)
            .withTeam(teamC)
            .withRole(Role.FINANCE_ADMINISTRATOR)
            .build()
    );

    var wuaIds = Set.of(
        new WebUserAccountId(1),
        new WebUserAccountId(2),
        new WebUserAccountId(3)
    );

    var energyPortalUsers = List.of(
        EnergyPortalUserDtoTestUtil.Builder()
            .withWebUserAccountId(1L)
            .withForename("AAAA")
            .build(),
        EnergyPortalUserDtoTestUtil.Builder()
            .withWebUserAccountId(2L)
            .withForename("CCCC")
            .build(),
        EnergyPortalUserDtoTestUtil.Builder()
            .withWebUserAccountId(3L)
            .withForename("BBBB")
            .build()
    );

    when(energyPortalUserService.findByWuaIds(wuaIds)).thenReturn(energyPortalUsers);

    var expectedTeamMemberViews = List.of(
        TeamMemberView.from(
            energyPortalUsers.getFirst(),
            teamA.getId(),
            Set.of(Role.CASE_OFFICER, Role.CASE_MANAGER, Role.ACCESS_MANAGER)
        ),
        TeamMemberView.from(
            energyPortalUsers.get(2),
            teamC.getId(),
            Set.of(Role.FINANCE_ADMINISTRATOR)
        ),
        TeamMemberView.from(
            energyPortalUsers.get(1),
            teamB.getId(),
            Set.of(Role.CONSENT_RECIPIENT)
        )
    );

    assertThat(teamMemberViewQueryService.getTeamMemberViews(teamRoles)).isEqualTo(expectedTeamMemberViews);
  }

  @Test
  void getTeamMemberViews_wuaIdNotFound() {
    var teamRoles = List.of(TeamRoleTestUtil.newBuilder().build());
    var webUserAccountId = new WebUserAccountId(teamRoles.getFirst().getWuaId());

    when(energyPortalUserService.findByWuaIds(Set.of(webUserAccountId))).thenReturn(List.of());

    assertThatThrownBy(() -> teamMemberViewQueryService.getTeamMemberViews(teamRoles))
        .isInstanceOf(TeamManagementException.class);
  }

  @Test
  void getTeamMemberViews_sameUserInMultipleTeams() {
    var teamA = TeamTestUtil.newBuilder().build();
    var teamB = TeamTestUtil.newBuilder().build();
    var teamC = TeamTestUtil.newBuilder().build();

    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withWuaId(1L)
            .withTeam(teamA)
            .withRole(Role.CASE_MANAGER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(1L)
            .withTeam(teamA)
            .withRole(Role.CASE_OFFICER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(1L)
            .withTeam(teamA)
            .withRole(Role.ACCESS_MANAGER)
            .build(),

        TeamRoleTestUtil.newBuilder()
            .withWuaId(1L)
            .withTeam(teamB)
            .withRole(Role.CONSENT_RECIPIENT)
            .build(),

        TeamRoleTestUtil.newBuilder()
            .withWuaId(1L)
            .withTeam(teamC)
            .withRole(Role.FINANCE_ADMINISTRATOR)
            .build()
    );

    var wuaIds = Set.of(new WebUserAccountId(1));

    var energyPortalUsers = List.of(
        EnergyPortalUserDtoTestUtil.Builder().withWebUserAccountId(1L).build()
    );

    when(energyPortalUserService.findByWuaIds(wuaIds)).thenReturn(energyPortalUsers);

    var expectedTeamMemberViews = List.of(
        TeamMemberView.from(
            energyPortalUsers.getFirst(),
            teamA.getId(),
            Set.of(Role.CASE_OFFICER, Role.CASE_MANAGER, Role.ACCESS_MANAGER)
        ),
        TeamMemberView.from(
            energyPortalUsers.getFirst(),
            teamB.getId(),
            Set.of(Role.CONSENT_RECIPIENT)
        ),
        TeamMemberView.from(
            energyPortalUsers.getFirst(),
            teamC.getId(),
            Set.of(Role.FINANCE_ADMINISTRATOR)
        )
    );

    assertThat(teamMemberViewQueryService.getTeamMemberViews(teamRoles))
        .containsExactlyInAnyOrderElementsOf(expectedTeamMemberViews);
  }

  @Test
  void getTeamMemberViews_userHasNoRoles() {
    var teamRoles = List.of(TeamRoleTestUtil.newBuilder().withRole(null).build());
    var webUserAccountId = new WebUserAccountId(teamRoles.getFirst().getWuaId());
    var energyPortalUsers = List.of(
        EnergyPortalUserDtoTestUtil.Builder().withWebUserAccountId(webUserAccountId).build()
    );

    when(energyPortalUserService.findByWuaIds(Set.of(webUserAccountId))).thenReturn(energyPortalUsers);

    assertThat(teamMemberViewQueryService.getTeamMemberViews(teamRoles))
        .containsExactly(TeamMemberView.from(
            energyPortalUsers.getFirst(),
            teamRoles.getFirst().getTeam().getId(),
            Set.of()
        ));
  }
}