package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus.SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ACCESS_MANGER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CASE_OFFICER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CASE_OFFICER_TEAM_MEMBER_VIEW_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_3;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TEAM_MEMBER_VIEW_LIST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.VIEWER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentService.USER_NOT_IN_CASE_OFFICER_ROLE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_TAKE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMember;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentServiceTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  private static final ServiceUserDetail USER2 = ServiceUserDetailTestUtil.Builder().withWuaId(2L).build();

  private static final WebUserAccountId WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER);

  private static final Team REGULATOR_TEAM = TeamTestUtil.Builder().build();

  private static final TeamMember CASE_OFFICER_1 = TeamMemberTestUtil.Builder()
      .withWebUserAccountId(1L)
      .withRole(CASE_OFFICER)
      .build();

  private static final TeamMember CASE_OFFICER_2 = TeamMemberTestUtil.Builder()
      .withWebUserAccountId(2L)
      .withRole(CASE_OFFICER)
      .build();

  private static final TeamMember CASE_OFFICER_3 = TeamMemberTestUtil.Builder()
      .withWebUserAccountId(3L)
      .withRole(CASE_OFFICER)
      .build();

  private static final List<Long> ASSIGNED_CASE_OFFICERS_WUA_IDS = List.of(CASE_OFFICER_1.wuaId().id(), CASE_OFFICER_2.wuaId().id());

  private static final List<WebUserAccountId> TEAM_CASE_OFFICER_WUA_IDS = List.of(CASE_OFFICER_1.wuaId(), CASE_OFFICER_2.wuaId());

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private RegulatorTeamService regulatorTeamService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;
  
  @Mock
  private TeamService teamService;

  @InjectMocks
  private CaseAssignmentService caseAssignmentService;

  private ApplicationVersion applicationVersion;

  private Collection<WebUserAccountId> allCaseOfficersWuaIds;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    allCaseOfficersWuaIds = new HashSet<>();
    allCaseOfficersWuaIds.add(CASE_OFFICER_1.wuaId());
    allCaseOfficersWuaIds.add(CASE_OFFICER_2.wuaId());
  }

  @Test
  void assignCaseOfficer_whenNotInCaseOfficerRole_thenThrowException() {
    when(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID))
        .thenReturn(false);

    assertThatThrownBy(() ->
        caseAssignmentService.assignCaseOfficer(applicationVersion, USER, USER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_NOT_IN_CASE_OFFICER_ROLE.apply(String.valueOf(USER.wuaId())));
  }

  @Test
  void assignCaseOfficer_whenInCaseOfficerRoleAndTakingOwnership_thenApplicationVersionCaseOfficerWuaUpdated() {
    when(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID))
        .thenReturn(true);

    caseAssignmentService.assignCaseOfficer(applicationVersion, USER, USER);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository, times(1))
        .save(applicationVersionArgumentCaptor.capture());

    assertThat(applicationVersionArgumentCaptor.getValue().getCaseOfficerWuaId())
        .isEqualTo(WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, USER, CASE_OFFICER_TAKE_OWNERSHIP, REGULATOR);
  }

  @Test
  void assignCaseOfficer_whenInCaseOfficerRoleAndAssigningOwnership_thenApplicationVersionCaseOfficerWuaUpdated() {
    when(regulatorTeamService.isCaseOfficer(WEB_USER_ACCOUNT_ID))
        .thenReturn(true);

    caseAssignmentService.assignCaseOfficer(applicationVersion, USER, USER2);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository, times(1))
        .save(applicationVersionArgumentCaptor.capture());

    assertThat(applicationVersionArgumentCaptor.getValue().getCaseOfficerWuaId())
        .isEqualTo(WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, USER2, CASE_OFFICER_ASSIGN_OWNERSHIP, REGULATOR);
  }

  @Test
  void unassignCaseOfficer_thenApplicationVersionCaseOfficerWuaNulled() {
    applicationVersion.setCaseOfficerWuaId(1L);
    caseAssignmentService.unassignCaseOfficer(applicationVersion, USER);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository, times(1))
        .save(applicationVersionArgumentCaptor.capture());

    assertThat(applicationVersionArgumentCaptor.getValue().getCaseOfficerWuaId())
        .isNull();

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, USER, CASE_OFFICER_RELEASE_OWNERSHIP, REGULATOR);
  }

  @Test
  void getCaseOfficerCandidates_whenUserNotRegulatorTeam_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.empty());

    assertThat(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getCaseOfficerCandidates_whenRegulatorUserAndCaseOfficersExist() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, USER))
        .containsExactly(CASE_OFFICER_TEAM_MEMBER_VIEW_1, CASE_OFFICER_TEAM_MEMBER_VIEW_2);
  }

  @Test
  void getCaseOfficerCandidates_whenRegulatorUserAndCaseOfficersExistAndExistingCaseOfficer() {
    applicationVersion.setCaseOfficerWuaId(CASE_OFFICER_TEAM_MEMBER_VIEW_1.wuaId().id());

    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, USER))
        .containsExactly(CASE_OFFICER_TEAM_MEMBER_VIEW_2);
  }

  @Test
  void getCaseOfficerCandidates_whenRegulatorUserButNoMembersExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(Collections.emptyList());

    assertThat(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getCaseOfficerCandidates_whenRegulatorUserAndCaseOfficersDontExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(List.of(VIEWER_TEAM_MEMBER_VIEW, ACCESS_MANGER_TEAM_MEMBER_VIEW));

    assertThat(caseAssignmentService.getCaseOfficerAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getCurrentCaseOfficers_whenUserNoRegulatorInCaseOfficerRole() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_OFFICER)))
        .thenReturn(Collections.emptyList());
    when(applicationVersionRepository
        .findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED)).thenReturn(ASSIGNED_CASE_OFFICERS_WUA_IDS);
    when(energyPortalUserService.findByWuaIds(allCaseOfficersWuaIds))
        .thenReturn(List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2));

    assertThat(caseAssignmentService.getCurrentCaseOfficers())
        .containsExactly(
            ENERGY_PORTAL_USER_1,
            ENERGY_PORTAL_USER_2
        );
  }

  @Test
  void getCurrentCaseOfficers_withNoCurrentNorPreviouslyAssignedCaseOfficersAvailable() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_OFFICER)))
        .thenReturn(Collections.emptyList());
    when(applicationVersionRepository
        .findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED))
        .thenReturn(Collections.emptyList());

    assertThat(caseAssignmentService.getCurrentCaseOfficers()).isEmpty();
  }

  @Test
  void getCurrentCaseOfficers_withCurrentCaseOfficersSameAsAssigned() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_OFFICER)))
        .thenReturn(TEAM_CASE_OFFICER_WUA_IDS);
    when(applicationVersionRepository
        .findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED)).thenReturn(ASSIGNED_CASE_OFFICERS_WUA_IDS);
    when(energyPortalUserService.findByWuaIds(allCaseOfficersWuaIds))
        .thenReturn(List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2));

    assertThat(caseAssignmentService.getCurrentCaseOfficers())
        .containsExactly(
            ENERGY_PORTAL_USER_1,
            ENERGY_PORTAL_USER_2
        );
  }

  @Test
  void getCurrentCaseOfficers_withCurrentCaseOfficersButNoneAssigned() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_OFFICER)))
        .thenReturn(TEAM_CASE_OFFICER_WUA_IDS);
    when(applicationVersionRepository
        .findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED)).thenReturn(Collections.emptyList());
    when(energyPortalUserService.findByWuaIds(allCaseOfficersWuaIds))
        .thenReturn(List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2));

    assertThat(caseAssignmentService.getCurrentCaseOfficers())
        .containsExactly(
            ENERGY_PORTAL_USER_1,
            ENERGY_PORTAL_USER_2
        );
  }

  @Test
  void getCurrentCaseOfficers_withPreviouslyAssignedCaseOfficers() {
    when(teamService.getWuaIdsOfTeamMembersWithRoles(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_OFFICER)))
        .thenReturn(TEAM_CASE_OFFICER_WUA_IDS);
    when(applicationVersionRepository
        .findAllCaseOfficerWuaIdsByApplicationVersionStatus(SUBMITTED)).thenReturn(List.of(CASE_OFFICER_3.wuaId().id()));

    allCaseOfficersWuaIds.add(CASE_OFFICER_3.wuaId());
    when(energyPortalUserService.findByWuaIds(allCaseOfficersWuaIds))
        .thenReturn(List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2, ENERGY_PORTAL_USER_3));

    assertThat(caseAssignmentService.getCurrentCaseOfficers())
        .containsExactly(
            ENERGY_PORTAL_USER_1,
            ENERGY_PORTAL_USER_2,
            ENERGY_PORTAL_USER_3
        );
  }

  @Test
  void getActiveCaseOfficers() {
    var caseOfficers = List.of(ENERGY_PORTAL_USER_1, ENERGY_PORTAL_USER_2, ENERGY_PORTAL_USER_3);
    var caseOfficerWebUserAccountIds = caseOfficers
        .stream()
        .map(EnergyPortalUserDto::webUserAccountId)
        .map(WebUserAccountId::new)
        .toList();

    when(teamService.getWuaIdsOfTeamMembersWithRoles(TeamType.REGULATOR, Set.of(CASE_OFFICER))).thenReturn(caseOfficerWebUserAccountIds);
    when(energyPortalUserService.findByWuaIds(caseOfficerWebUserAccountIds)).thenReturn(caseOfficers);

    assertThat(caseAssignmentService.getActiveCaseOfficers()).isEqualTo(caseOfficers);
  }
}
