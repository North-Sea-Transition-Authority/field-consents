package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.USER_WUA_ID;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CAM_USER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.CAM_USER_TEAM_MEMBER_VIEW_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam.CamAssignmentService.USER_NOT_IN_CAM_ROLE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CAM_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.teams.Role.CONSENTS_AND_AUTHORISATIONS_MANAGER;

import java.util.List;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEmailService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class CamAssignmentServiceTest {
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  
  private static final ServiceUserDetail CAM_USER = ServiceUserDetailTestUtil.Builder().withWuaId(2L).build();

  private static final WebUserAccountId WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER);
  
  private static final WebUserAccountId WEB_CAM_USER_ACCOUNT_ID = WebUserAccountId.from(CAM_USER);

  private static final Team REGULATOR_TEAM = TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build();

  @Mock
  private ApplicationVersionRepository applicationVersionRepository;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private CaseAssignmentEmailService caseAssignmentEmailService;

  @InjectMocks
  private CamAssignmentService camAssignmentService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationVersion.setCaseOfficerWuaId(WEB_USER_ACCOUNT_ID.id());
    applicationVersion.setCurrentCaseOwner(Role.CASE_OFFICER);
  }

  @Test
  void assignCamUser_whenNotInCamRole_thenThrowException() {
    when(teamQueryService.userHasStaticRole(CAM_USER, TeamType.REGULATOR, CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(false);

    assertThatThrownBy(() ->
        camAssignmentService.assignCamUser(applicationVersion, CAM_USER, USER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_NOT_IN_CAM_ROLE.apply(String.valueOf(CAM_USER.wuaId())));

    verify(caseAssignmentEmailService, never()).sendCaseAssignmentEmail(any(), any(), any(), any());
  }

  @Test
  void assignCamUser_whenInCamRole_thenApplicationVersionCamWuaId() {
    when(teamQueryService.userHasStaticRole(CAM_USER, TeamType.REGULATOR, CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(true);

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

    verify(caseAssignmentEmailService).sendCaseAssignmentEmail(
        applicationVersion,
        GovukNotifyTemplate.CASE_ASSIGNED_TO_CAM_USER,
        FieldConsentsEmailRecipient.from(CAM_USER),
        USER);
  }

  @Test
  void assignCamUser_whenSendCamAssignmentEmailFails_thenApplicationVersionCamDetailsAreStillUpdated() {
    when(teamQueryService.userHasStaticRole(CAM_USER, TeamType.REGULATOR, CONSENTS_AND_AUTHORISATIONS_MANAGER)).thenReturn(true);

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(caseAssignmentEmailService)
        .sendCaseAssignmentEmail(
            applicationVersion,
            GovukNotifyTemplate.CASE_ASSIGNED_TO_CAM_USER,
            FieldConsentsEmailRecipient.from(CAM_USER),
            USER);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> camAssignmentService.assignCamUser(applicationVersion, CAM_USER, USER)
    );

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

    verify(caseAssignmentEmailService).sendCaseAssignmentEmail(
        applicationVersion,
        GovukNotifyTemplate.CASE_ASSIGNED_TO_CAM_USER,
        FieldConsentsEmailRecipient.from(CAM_USER),
        USER);
  }

  @Test
  void getCamUserAssignmentCandidates_whenUserNotRegulatorTeam_thenEmpty() {
    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion)).isEmpty();
  }

  @Test
  void getCamUserAssignmentCandidates_whenRegulatorUserAndCurrentlyAssignedCamUserIsExcluded() {
    applicationVersion.setCamWuaId(CAM_USER_TEAM_MEMBER_VIEW_1.wuaId());

    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(REGULATOR_TEAM)
            .withWuaId(CAM_USER_TEAM_MEMBER_VIEW_1.wuaId())
            .withRole(CONSENTS_AND_AUTHORISATIONS_MANAGER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(REGULATOR_TEAM)
            .withWuaId(CAM_USER_TEAM_MEMBER_VIEW_2.wuaId())
            .withRole(CONSENTS_AND_AUTHORISATIONS_MANAGER)
            .build()
    );

    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(teamRoles);

    when(teamQueryService.getTeamMemberViews(List.of(teamRoles.getLast())))
        .thenReturn(List.of(CAM_USER_TEAM_MEMBER_VIEW_2));

    assertThat(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion))
        .containsExactly(CAM_USER_TEAM_MEMBER_VIEW_2);
  }

  @Test
  void getCamUserAssignmentCandidates_whenRegulatorUserButNoMembersExist_thenEmpty() {
    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(camAssignmentService.getCamUserAssignmentCandidates(applicationVersion)).isEmpty();
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
