package uk.co.nstauthority.fieldconsents.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ACCESS_MANGER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CAM_USER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CAM_USER_TEAM_MEMBER_VIEW_2;
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

  private final static Team TEST_TEAM = TeamTestUtil.Builder().build();

  private final static TeamMember TEAM_MEMBER_1 = TeamMemberTestUtil.Builder()
      .withWebUserAccountId(1)
      .build();

  private final static TeamMember TEAM_MEMBER_2 = TeamMemberTestUtil.Builder()
      .withWebUserAccountId(2)
      .build();

  @Mock
  private TeamMemberService teamMemberService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private TeamService teamService;

  @InjectMocks
  private TeamMemberViewService teamMemberViewService;

  @Test
  void getTeamMemberViewsForTeam_verifyTeamMemberViewMapping() {

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(RegulatorTeamRole.ACCESS_MANAGER)
        .build();

    when(teamMemberService.getTeamMembers(TEST_TEAM)).thenReturn(List.of(teamMember));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMember.wuaId().id())
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMember.wuaId())))
        .thenReturn(Map.of(WebUserAccountId.from(energyPortalUser.webUserAccountId()), energyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(TEST_TEAM);

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
    when(teamMemberService.getTeamMembers(TEST_TEAM)).thenReturn(List.of(TEAM_MEMBER_2, TEAM_MEMBER_1));

    var firstAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(TEAM_MEMBER_1.wuaId().id())
        .withForename("A forename")
        .withSurname("A surname")
        .build();

    var secondAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(TEAM_MEMBER_2.wuaId().id())
        .withForename("B forename")
        .withSurname("B surname")
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(TEAM_MEMBER_2.wuaId(), TEAM_MEMBER_1.wuaId())))
        .thenReturn(Map.of(
            WebUserAccountId.from(firstAlphabeticallyEnergyPortalUser.webUserAccountId()), firstAlphabeticallyEnergyPortalUser,
            WebUserAccountId.from(secondAlphabeticallyEnergyPortalUser.webUserAccountId()), secondAlphabeticallyEnergyPortalUser
        ));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(TEST_TEAM);

    assertThat(resultingTeamMemberViews)
        .extracting(TeamMemberView::firstName, TeamMemberView::lastName)
        .containsExactly(
            tuple(firstAlphabeticallyEnergyPortalUser.forename(), firstAlphabeticallyEnergyPortalUser.surname()),
            tuple(secondAlphabeticallyEnergyPortalUser.forename(), secondAlphabeticallyEnergyPortalUser.surname())
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenMultipleTeamMembersWithSameForename_verifyOrderedBySurname() {

    when(teamMemberService.getTeamMembers(TEST_TEAM)).thenReturn(List.of(TEAM_MEMBER_2, TEAM_MEMBER_1));

    var firstAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(TEAM_MEMBER_1.wuaId().id())
        .withForename("A forename")
        .withSurname("A surname")
        .build();

    var secondAlphabeticallyEnergyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(TEAM_MEMBER_2.wuaId().id())
        .withForename("A forename")
        .withSurname("B surname")
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(TEAM_MEMBER_2.wuaId(), TEAM_MEMBER_1.wuaId())))
        .thenReturn(Map.of(
            WebUserAccountId.from(firstAlphabeticallyEnergyPortalUser.webUserAccountId()), firstAlphabeticallyEnergyPortalUser,
            WebUserAccountId.from(secondAlphabeticallyEnergyPortalUser.webUserAccountId()), secondAlphabeticallyEnergyPortalUser
        ));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(TEST_TEAM);

    assertThat(resultingTeamMemberViews)
        .extracting(TeamMemberView::firstName, TeamMemberView::lastName)
        .containsExactly(
            tuple(firstAlphabeticallyEnergyPortalUser.forename(), firstAlphabeticallyEnergyPortalUser.surname()),
            tuple(secondAlphabeticallyEnergyPortalUser.forename(), secondAlphabeticallyEnergyPortalUser.surname())
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenMultipleRoles_verifyOrderedByRoleDisplayOrder() {

    var teamMemberWithMultipleRoles = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.SECOND_ROLE_BY_DISPLAY_ORDER)
        .withRole(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER)
        .build();

    when(teamMemberService.getTeamMembers(TEST_TEAM)).thenReturn(List.of(teamMemberWithMultipleRoles));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMemberWithMultipleRoles.wuaId().id())
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMemberWithMultipleRoles.wuaId())))
        .thenReturn(Map.of(WebUserAccountId.from(energyPortalUser.webUserAccountId()), energyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService.getTeamMemberViewsForTeam(TEST_TEAM);

    assertThat(resultingTeamMemberViews).hasSize(1);
    assertThat(resultingTeamMemberViews.get(0).teamRoles())
        .containsExactly(
            TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER,
            TestTeamRole.SECOND_ROLE_BY_DISPLAY_ORDER
        );
  }

  @Test
  void getTeamMemberViewsForTeam_whenNoEnergyPortalUserFound_thenException() {

    var teamMember = TeamMemberTestUtil.Builder()
        .build();

    when(teamMemberService.getTeamMembers(TEST_TEAM)).thenReturn(List.of(teamMember));

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMember.wuaId())))
        .thenReturn(Collections.emptyMap());

    assertThatThrownBy(
        () -> teamMemberViewService.getTeamMemberViewsForTeam(TEST_TEAM)
    )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(
            "Did not find an Energy Portal User with WUA ID %s when converting team members"
            .formatted(teamMember.wuaId())
        );
  }

  @Test
  void getTeamMemberViewsWithRolesForTeamType_whenNoTeamOfTypeIsFound() {
    when(teamService.getTeamsByType(any())).thenReturn(Collections.emptyList());

    assertThat(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TEST_TEAM.getTeamType(), Set.of(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER)))
        .isEmpty();
  }

  @Test
  void getTeamMemberViewsWithRolesForTeamType_whenNoMemberInTeamTypeIsFound() {
    when(teamService.getTeamsByType(any())).thenReturn(List.of(TEST_TEAM));
    when(teamMemberService.getTeamMembers(any())).thenReturn(Collections.emptyList());

    assertThat(teamMemberViewService.
        getTeamMemberViewsWithRolesForTeamType(TEST_TEAM.getTeamType(), Set.of(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER))).isEmpty();
  }

  @Test
  void getTeamMemberViewsWithRolesForTeamType_whenOneMemberWithTeamRoleInTeamTypeIsFound() {
    when(teamService.getTeamsByType(any())).thenReturn(List.of(TEST_TEAM));

    var teamMember = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER)
        .build();

    when(teamMemberService.getTeamMembers(TEST_TEAM)).thenReturn(List.of(teamMember));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMember.wuaId().id())
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMember.wuaId())))
        .thenReturn(Map.of(WebUserAccountId.from(energyPortalUser.webUserAccountId()), energyPortalUser));


    var resultingTeamMemberViews = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TEST_TEAM.getTeamType(), Set.of(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER));

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
  void getTeamMemberViewsWithRolesForTeamType_whenMultipleTeamMembersWithTeamRoleInTeamTypeAreFound() {
    when(teamService.getTeamsByType(any())).thenReturn(List.of(TEST_TEAM));

    var teamMember1 = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(TEAM_MEMBER_1.wuaId().id())
        .withRole(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER)
        .build();

    var teamMember2 = TeamMemberTestUtil.Builder()
        .withWebUserAccountId(TEAM_MEMBER_2.wuaId().id())
        .withRole(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER)
        .build();

    when(teamMemberService.getTeamMembers(TEST_TEAM)).thenReturn(List.of(teamMember2, teamMember1));

    var energyPortalUser1 = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMember1.wuaId().id())
        .build();

    var energyPortalUser2 = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMember2.wuaId().id())
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMember2.wuaId(), teamMember1.wuaId())))
        .thenReturn(Map.of(
            WebUserAccountId.from(energyPortalUser1.webUserAccountId()), energyPortalUser1,
            WebUserAccountId.from(energyPortalUser2.webUserAccountId()), energyPortalUser2
        ));

    var resultingTeamMemberViews = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TEST_TEAM.getTeamType(), Set.of(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER));

    assertThat(resultingTeamMemberViews)
        .extracting(TeamMemberView::firstName, TeamMemberView::lastName)
        .containsExactly(
            tuple(energyPortalUser1.forename(), energyPortalUser1.surname()),
            tuple(energyPortalUser2.forename(), energyPortalUser2.surname())
        );
  }

  @Test
  void getTeamMemberViewsWithRolesForTeamType_whenTeamMemberWithMultipleRolesInTeamTypeIsFound() {
    when(teamService.getTeamsByType(any())).thenReturn(List.of(TEST_TEAM));

    var teamMemberWithMultipleRoles = TeamMemberTestUtil.Builder()
        .withRole(TestTeamRole.SECOND_ROLE_BY_DISPLAY_ORDER)
        .withRole(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER)
        .build();

    when(teamMemberService.getTeamMembers(TEST_TEAM)).thenReturn(List.of(teamMemberWithMultipleRoles));

    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder()
        .withWebUserAccountId(teamMemberWithMultipleRoles.wuaId().id())
        .build();

    when(energyPortalUserService.getEnergyPortalUserMap(List.of(teamMemberWithMultipleRoles.wuaId())))
        .thenReturn(Map.of(WebUserAccountId.from(energyPortalUser.webUserAccountId()), energyPortalUser));

    var resultingTeamMemberViews = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TEST_TEAM.getTeamType(), Set.of(TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER));

    assertThat(resultingTeamMemberViews).hasSize(1);
    assertThat(resultingTeamMemberViews.get(0).teamRoles())
        .containsExactly(
            TestTeamRole.FIRST_ROLE_BY_DISPLAY_ORDER,
            TestTeamRole.SECOND_ROLE_BY_DISPLAY_ORDER
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
                TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2.getDisplayName()),
            entry(CAM_USER_TEAM_MEMBER_VIEW_1.wuaId().toString(),
                CAM_USER_TEAM_MEMBER_VIEW_1.getDisplayName()),
            entry(CAM_USER_TEAM_MEMBER_VIEW_2.wuaId().toString(),
                CAM_USER_TEAM_MEMBER_VIEW_2.getDisplayName())
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
