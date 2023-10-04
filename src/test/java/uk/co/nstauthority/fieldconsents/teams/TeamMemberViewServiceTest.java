package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ACCESS_MANGER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CASE_OFFICER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CASE_OFFICER_TEAM_MEMBER_VIEW_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.VIEWER_TEAM_MEMBER_VIEW;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class TeamMemberViewServiceTest {

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private TeamMemberViewService teamMemberViewService;

  @Test
  void getTeamMemberViewsForTeam_verifyTeamMemberViewMapping() {

    var team = TeamTestUtil.Builder().build();

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(teamMember));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMember.wuaId().id())
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMember.wuaId())))
        .thenReturn(Map.of(WebUserAccountId.from(energyPortalUser.webUserAccountId()), energyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(resultingTeamMemberViews).extracting(
        TeamMemberView::wuaId,
        TeamMemberView::title,
        TeamMemberView::firstName,
        TeamMemberView::lastName,

        TeamMemberView::contactEmail,
        TeamMemberView::contactNumber,
        TeamMemberView::teamRoles
    ).containsExactly(
        Tuple.tuple(
            teamMember.wuaId(),
            energyPortalUser.title(),
            energyPortalUser.forename(),
            energyPortalUser.surname(),
            energyPortalUser.emailAddress(),
            energyPortalUser.telephoneNumber(),
            teamMember.roles()
        )
    );
  }

  @Test
  void getTeamMemberViewsForTeam_whenMultipleTeamMembers_verifyOrderedByName() {

    var team = TeamTestUtil.Builder().build();

    var firstTeamMember = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(1)
        .build();

    var secondTeamMember = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(2)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(secondTeamMember, firstTeamMember));

    var firstAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(firstTeamMember.wuaId().id())
        .withForename("A forename")
        .withSurname("A surname")
        .build();

    var secondAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(secondTeamMember.wuaId().id())
        .withForename("B forename")
        .withSurname("B surname")
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(secondTeamMember.wuaId(), firstTeamMember.wuaId())))
        .thenReturn(Map.of(
            WebUserAccountId.from(firstAlphabeticallyEnergyPortalUser.webUserAccountId()), firstAlphabeticallyEnergyPortalUser,
            WebUserAccountId.from(secondAlphabeticallyEnergyPortalUser.webUserAccountId()), secondAlphabeticallyEnergyPortalUser
        ));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(resultingTeamMemberViews)
        .extracting(TeamMemberView::firstName, TeamMemberView::lastName)
        .containsExactly(
            tuple(firstAlphabeticallyEnergyPortalUser.forename(), firstAlphabeticallyEnergyPortalUser.surname()),
            tuple(secondAlphabeticallyEnergyPortalUser.forename(), secondAlphabeticallyEnergyPortalUser.surname())
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenMultipleTeamMembersWithSameForename_verifyOrderedBySurname() {

    var team = TeamTestUtil.Builder().build();

    var firstTeamMember = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(1)
        .build();

    var secondTeamMember = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(2)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(secondTeamMember, firstTeamMember));

    var firstAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(firstTeamMember.wuaId().id())
        .withForename("A forename")
        .withSurname("A surname")
        .build();

    var secondAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(secondTeamMember.wuaId().id())
        .withForename("A forename")
        .withSurname("B surname")
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(secondTeamMember.wuaId(), firstTeamMember.wuaId())))
        .thenReturn(Map.of(
            WebUserAccountId.from(firstAlphabeticallyEnergyPortalUser.webUserAccountId()), firstAlphabeticallyEnergyPortalUser,
            WebUserAccountId.from(secondAlphabeticallyEnergyPortalUser.webUserAccountId()), secondAlphabeticallyEnergyPortalUser
        ));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(resultingTeamMemberViews)
        .extracting(TeamMemberView::firstName, TeamMemberView::lastName)
        .containsExactly(
            tuple(firstAlphabeticallyEnergyPortalUser.forename(), firstAlphabeticallyEnergyPortalUser.surname()),
            tuple(secondAlphabeticallyEnergyPortalUser.forename(), secondAlphabeticallyEnergyPortalUser.surname())
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenMultipleRoles_verifyOrderedByRoleDisplayOrder() {

    var team = TeamTestUtil.Builder().build();

    var teamMemberWithMultipleRoles = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.SECOND_ROLE_BY_DISPLAY_ORDER)
        .withRole(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER)
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(teamMemberWithMultipleRoles));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMemberWithMultipleRoles.wuaId().id())
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMemberWithMultipleRoles.wuaId())))
        .thenReturn(Map.of(WebUserAccountId.from(energyPortalUser.webUserAccountId()), energyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(team);

    assertThat(resultingTeamMemberViews).hasSize(1);
    assertThat(resultingTeamMemberViews.get(0).teamRoles())
        .containsExactly(
            TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER,
            TestTeamRole.SECOND_ROLE_BY_DISPLAY_ORDER
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenNoEnergyPortalUserFound_thenException() {

    var team = TeamTestUtil.Builder().build();

    var teamMember = TeamMemberTestUtil.Builder()
        .build();

    when(teamMemberService.getTeamMembers(team)).thenReturn(List.of(teamMember));

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMember.wuaId())))
        .thenReturn(Collections.emptyMap());

    assertThatThrownBy(
        () -> teamMemberViewService.getTeamMemberViewsForTeam(team)
    )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "Did not find an Energy Portal User with WUA ID %s when converting team members"
            .formatted(teamMember.wuaId())
        );
  }

  @Test
  void getUsersMap_many() {
    assertThat(teamMemberViewService.getUsersMap(AssignmentTestUtil.TEAM_MEMBER_VIEW_LIST))
        .containsOnly(
            entry(CASE_OFFICER_TEAM_MEMBER_VIEW_1.wuaId().toString(),
                CASE_OFFICER_TEAM_MEMBER_VIEW_1.getDisplayName()),
            entry(VIEWER_TEAM_MEMBER_VIEW.wuaId().toString(),
                VIEWER_TEAM_MEMBER_VIEW.getDisplayName()),
            entry(CASE_OFFICER_TEAM_MEMBER_VIEW_2.wuaId().toString(),
                CASE_OFFICER_TEAM_MEMBER_VIEW_2.getDisplayName()),
            entry(ACCESS_MANGER_TEAM_MEMBER_VIEW.wuaId().toString(),
                ACCESS_MANGER_TEAM_MEMBER_VIEW.getDisplayName()),
            entry(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1.wuaId().toString(),
                TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1.getDisplayName()),
            entry(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2.wuaId().toString(),
                TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2.getDisplayName())
        );
  }

  @Test
  void getUsersMap_one() {
    assertThat(teamMemberViewService.getUsersMap(List.of(CASE_OFFICER_TEAM_MEMBER_VIEW_1)))
        .containsOnly(
            entry(CASE_OFFICER_TEAM_MEMBER_VIEW_1.wuaId().toString(), CASE_OFFICER_TEAM_MEMBER_VIEW_1.getDisplayName())
        );
  }

  @Test
  void getUsersMap_none() {
    assertThat(teamMemberViewService.getUsersMap(Collections.emptyList()))
        .isEmpty();
  }

  enum TestTeamRole implements TeamRole {

    FIRST_ROLE_BY_DISPLAY_ORDER(1),
    SECOND_ROLE_BY_DISPLAY_ORDER(2);

    private final int displayOrder;

    TestTeamRole(int displayOrder) {
      this.displayOrder = displayOrder;
    }

    @Override
    public String getDisplayName() {
      return "";
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public int getDisplayOrder() {
      return displayOrder;
    }

    @Override
    public Set<RolePermission> getRolePermissions() {
      return Set.of();
    }
  }
}
