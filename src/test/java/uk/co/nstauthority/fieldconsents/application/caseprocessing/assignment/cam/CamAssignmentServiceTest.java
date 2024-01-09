package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ACCESS_MANGER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CAM_USER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CAM_USER_TEAM_MEMBER_VIEW_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TEAM_MEMBER_VIEW_LIST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.VIEWER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam.CamAssignmentService.USER_NOT_IN_CAM_ROLE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CAM_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMember;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ExtendWith(MockitoExtension.class)
class CamAssignmentServiceTest {
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  
  private static final ServiceUserDetail CAM_USER = ServiceUserDetailTestUtil.Builder().withWuaId(2L).build();

  private static final WebUserAccountId WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER);
  
  private static final WebUserAccountId WEB_CAM_USER_ACCOUNT_ID = WebUserAccountId.from(CAM_USER);

  private static final Team REGULATOR_TEAM = TeamTestUtil.Builder().build();

  private static final TeamMember CAM_USER_1 = TeamMemberTestUtil.Builder()
      .withWebUserAccountId(1L)
      .withRole(CONSENTS_AND_AUTHORISATIONS_MANAGER)
      .build();

  private static final TeamMember CAM_USER_2 = TeamMemberTestUtil.Builder()
      .withWebUserAccountId(2L)
      .withRole(CONSENTS_AND_AUTHORISATIONS_MANAGER)
      .build();
  
  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private RegulatorTeamService regulatorTeamService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @InjectMocks
  private CamAssignmentService camAssignmentService;

  private ApplicationVersion applicationVersion;

  private Collection<WebUserAccountId> allCamUsersWuaIds;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setCaseOfficerWuaId(WEB_USER_ACCOUNT_ID.id());
    applicationVersion.setCurrentCaseOwner(CASE_OFFICER);
    allCamUsersWuaIds = new HashSet<>();
    allCamUsersWuaIds.add(CAM_USER_1.wuaId());
    allCamUsersWuaIds.add(CAM_USER_2.wuaId());
  }

  @Test
  void assignCamUser_whenNotInCamRole_thenThrowException() {
    when(regulatorTeamService.isCamUser(WEB_CAM_USER_ACCOUNT_ID))
        .thenReturn(false);

    assertThatThrownBy(() ->
        camAssignmentService.assignCamUser(applicationVersion, CAM_USER, USER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_NOT_IN_CAM_ROLE.apply(String.valueOf(CAM_USER.wuaId())));
  }

  @Test
  void assignCamUser_whenInCamRole_thenApplicationVersionCamWuaId() {
    when(regulatorTeamService.isCamUser(WEB_CAM_USER_ACCOUNT_ID))
        .thenReturn(true);

    camAssignmentService.assignCamUser(applicationVersion, CAM_USER, USER);

    var applicationVersionArgumentCaptor = ArgumentCaptor.forClass(ApplicationVersion.class);

    verify(applicationVersionRepository, times(1))
        .save(applicationVersionArgumentCaptor.capture());

    assertThat(applicationVersionArgumentCaptor.getValue()).extracting(
        ApplicationVersion::getCaseOfficerWuaId,
        ApplicationVersion::getCamWuaId,
        ApplicationVersion::getCurrentCaseOwner
    ).contains(
        WEB_USER_ACCOUNT_ID.id(),
        WEB_CAM_USER_ACCOUNT_ID.id(),
        CONSENTS_AND_AUTHORISATIONS_MANAGER
    );

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, USER, CAM_ASSIGN_OWNERSHIP, REGULATOR);
  }
  
  @Test
  void getCamUserAssignmentCandidates_whenUserNotRegulatorTeam_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.empty());

    assertThat(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getCamUserAssignmentCandidates_whenRegulatorUserAndCamUsersExist() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, USER))
        .containsExactly(CAM_USER_TEAM_MEMBER_VIEW_1, CAM_USER_TEAM_MEMBER_VIEW_2);
  }

  @Test
  void getCamUserAssignmentCandidates_whenRegulatorUserAndCurrentlyAssignedCamUserIsExcluded() {
    applicationVersion.setCamWuaId(CAM_USER_TEAM_MEMBER_VIEW_1.wuaId().id());

    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, USER))
        .containsExactly(CAM_USER_TEAM_MEMBER_VIEW_2);
  }

  @Test
  void getCamUserAssignmentCandidates_whenRegulatorUserButNoMembersExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(Collections.emptyList());

    assertThat(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getCamUserAssignmentCandidates_whenRegulatorUserAndCamUsersDontExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(List.of(VIEWER_TEAM_MEMBER_VIEW, ACCESS_MANGER_TEAM_MEMBER_VIEW));

    assertThat(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void findCamWuaId_whenNotAssigned() {
    assertThat(camAssignmentService.findCamWuaId(applicationVersion))
        .isEmpty();
  }

  @Test
  void findCamWuaId_whenAssigned() {
    applicationVersion.setCamWuaId(USER_WUA_ID);
    assertThat(camAssignmentService.findCamWuaId(applicationVersion))
        .contains(WebUserAccountId.from(USER_WUA_ID));
  }
}
