package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_5;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_6;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewAssignmentService.USER_NOT_IN_TECHNICAL_REVIEWER_ROLE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEW_REQUEST;

import java.time.Clock;
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
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewAssignmentServiceTest {

  private static final ServiceUserDetail USER = SERVICE_USER_DETAIL_USER_5;
  private static final ServiceUserDetail USER2 = SERVICE_USER_DETAIL_USER_6;
  private static final WebUserAccountId USER_WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER);
  private static final WebUserAccountId USER2_WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER2);
  private static final Team REGULATOR_TEAM = TeamTestUtil.newBuilder().build();

  @Mock
  private Clock clock;

  @Mock
  private TechnicalReviewRepository technicalReviewRepository;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private TechnicalReviewEmailService technicalReviewEmailService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private TechnicalReviewAssignmentService technicalReviewAssignmentService;

  private TechnicalReview technicalReview;

  @BeforeEach
  void setUp() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, USER, clock);
  }

  @Test
  void assignTechnicalReviewer_whenNotInTechnicalReviewerRole_thenThrowException() {
    when(teamQueryService.userHasStaticRole(USER, TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)).thenReturn(false);

    assertThatThrownBy(() ->
        technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER, USER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_NOT_IN_TECHNICAL_REVIEWER_ROLE.apply(String.valueOf(USER.wuaId())));
  }

  @Test
  void assignTechnicalReviewer_whenInTechnicalReviewerRoleAndNoExistingTechnicalReviewer_thenTechnicalReviewerWuaUpdated() {
    technicalReview.setTechnicalReviewerWuaId(null);

    when(teamQueryService.userHasStaticRole(USER, TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)).thenReturn(true);

    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER, USER);

    var technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);

    verify(technicalReviewRepository)
        .save(technicalReviewArgumentCaptor.capture());

    var updatedTechnicalReview = technicalReviewArgumentCaptor.getValue();
    assertThat(updatedTechnicalReview.getTechnicalReviewerWuaId())
        .isEqualTo(USER_WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(updatedTechnicalReview.getRequestApplicationVersion(), USER,
            TECHNICAL_REVIEW_REQUEST, REGULATOR_TECHNICAL_REVIEWER);

    verify(technicalReviewEmailService).sendTechnicalReviewRequestEmail(technicalReview, USER);
  }

  @Test
  void assignTechnicalReviewer_whenInTechnicalReviewerRoleAndTakingOwnership_thenTechnicalReviewerWuaUpdated() {
    when(teamQueryService.userHasStaticRole(USER2, TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)).thenReturn(true);

    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER2, USER2);

    var technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);

    verify(technicalReviewRepository)
        .save(technicalReviewArgumentCaptor.capture());

    var updatedTechnicalReview = technicalReviewArgumentCaptor.getValue();
    assertThat(updatedTechnicalReview.getTechnicalReviewerWuaId())
        .isEqualTo(USER2_WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(updatedTechnicalReview.getRequestApplicationVersion(), USER2,
            TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP, REGULATOR_TECHNICAL_REVIEWER);

    verify(technicalReviewEmailService).sendTechnicalReviewRequestEmail(technicalReview, USER2);
  }

  @Test
  void assignTechnicalReviewer_whenInTechnicalReviewerRoleAndAssigningOwnership_thenTechnicalReviewerWuaUpdated() {
    when(teamQueryService.userHasStaticRole(USER2, TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)).thenReturn(true);

    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER2, USER);

    var technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);

    verify(technicalReviewRepository)
        .save(technicalReviewArgumentCaptor.capture());

    var updatedTechnicalReview = technicalReviewArgumentCaptor.getValue();
    assertThat(updatedTechnicalReview.getTechnicalReviewerWuaId())
        .isEqualTo(USER2_WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(updatedTechnicalReview.getRequestApplicationVersion(), USER,
            TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP, REGULATOR_TECHNICAL_REVIEWER);

    verify(technicalReviewEmailService).sendTechnicalReviewRequestEmail(technicalReview, USER);
  }

  @Test
  void assignTechnicalReviewer_whenSendTechnicalReviewRequestEmailFails_thenTechnicalReviewerWuaIdIsStillUpdated() {
    when(teamQueryService.userHasStaticRole(USER2, TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)).thenReturn(true);

    var technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(technicalReviewEmailService)
        .sendTechnicalReviewRequestEmail(technicalReview, USER);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(() -> technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER2, USER));

    verify(technicalReviewRepository)
        .save(technicalReviewArgumentCaptor.capture());

    var updatedTechnicalReview = technicalReviewArgumentCaptor.getValue();
    assertThat(updatedTechnicalReview.getTechnicalReviewerWuaId())
        .isEqualTo(USER2_WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService)
        .prioritiseApplicationInWorkArea(updatedTechnicalReview.getRequestApplicationVersion(), USER,
            TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP, REGULATOR_TECHNICAL_REVIEWER);

    verify(technicalReviewEmailService).sendTechnicalReviewRequestEmail(technicalReview, USER);
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenUserNotRegulatorTeam_thenEmpty() {
    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(List.of());
    assertThat(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates()).isEmpty();
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenTechnicalReviewersExist() {
    var teamRoles = List.of(
        TeamRoleTestUtil.newBuilder()
            .withWuaId(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1.wuaId())
            .withRole(Role.TECHNICAL_REVIEWER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withWuaId(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2.wuaId())
            .withRole(Role.TECHNICAL_REVIEWER)
            .build()
    );


    var teamMemberRoles = List.of(
        TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1, TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2
    );

    when(teamQueryService.getTeamRoles(TeamType.REGULATOR)).thenReturn(teamRoles);
    when(teamQueryService.getTeamMemberViews(teamRoles)).thenReturn(teamMemberRoles);

    assertThat(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates())
        .isEqualTo(teamMemberRoles);
  }
}
