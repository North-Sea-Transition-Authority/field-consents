package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ACCESS_MANGER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_5;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_6;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TEAM_MEMBER_VIEW_LIST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.VIEWER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewAssignmentService.USER_NOT_IN_TECHNICAL_REVIEWER_ROLE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEW_REQUEST;

import java.time.Clock;
import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewAssignmentServiceTest {

  private static final ServiceUserDetail USER = SERVICE_USER_DETAIL_USER_5;
  private static final ServiceUserDetail USER2 = SERVICE_USER_DETAIL_USER_6;
  private static final WebUserAccountId USER_WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER);
  private static final WebUserAccountId USER2_WEB_USER_ACCOUNT_ID = WebUserAccountId.from(USER2);
  private static final Team REGULATOR_TEAM = TeamTestUtil.Builder().build();

  @Mock
  private Clock clock;

  @Mock
  private TechnicalReviewRepository technicalReviewRepository;

  @Mock
  private RegulatorTeamService regulatorTeamService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

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
    when(regulatorTeamService.isTechnicalReviewer(USER_WEB_USER_ACCOUNT_ID))
        .thenReturn(false);

    assertThatThrownBy(() ->
        technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER, USER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(USER_NOT_IN_TECHNICAL_REVIEWER_ROLE.apply(String.valueOf(USER.wuaId())));
  }

  @Test
  void assignTechnicalReviewer_whenInTechnicalReviewerRoleAndNoExistingTechnicalReviewer_thenTechnicalReviewerWuaUpdated() {
    technicalReview.setTechnicalReviewerWuaId(null);

    when(regulatorTeamService.isTechnicalReviewer(USER_WEB_USER_ACCOUNT_ID))
        .thenReturn(true);

    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER, USER);

    var technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);

    verify(technicalReviewRepository, times(1))
        .save(technicalReviewArgumentCaptor.capture());

    var updatedTechnicalReview = technicalReviewArgumentCaptor.getValue();
    assertThat(updatedTechnicalReview.getTechnicalReviewerWuaId())
        .isEqualTo(USER_WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(updatedTechnicalReview.getApplicationVersion(), USER,
            TECHNICAL_REVIEW_REQUEST, REGULATOR_TECHNICAL_REVIEWER);
  }

  @Test
  void assignTechnicalReviewer_whenInTechnicalReviewerRoleAndTakingOwnership_thenTechnicalReviewerWuaUpdated() {
    when(regulatorTeamService.isTechnicalReviewer(USER2_WEB_USER_ACCOUNT_ID))
        .thenReturn(true);

    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER2, USER2);

    var technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);

    verify(technicalReviewRepository, times(1))
        .save(technicalReviewArgumentCaptor.capture());

    var updatedTechnicalReview = technicalReviewArgumentCaptor.getValue();
    assertThat(updatedTechnicalReview.getTechnicalReviewerWuaId())
        .isEqualTo(USER2_WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(updatedTechnicalReview.getApplicationVersion(), USER2,
            TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP, REGULATOR_TECHNICAL_REVIEWER);
  }

  @Test
  void assignTechnicalReviewer_whenInTechnicalReviewerRoleAndAssigningOwnership_thenTechnicalReviewerWuaUpdated() {
    when(regulatorTeamService.isTechnicalReviewer(USER2_WEB_USER_ACCOUNT_ID))
        .thenReturn(true);

    technicalReviewAssignmentService.assignTechnicalReviewer(technicalReview, USER2, USER);

    var technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);

    verify(technicalReviewRepository, times(1))
        .save(technicalReviewArgumentCaptor.capture());

    var updatedTechnicalReview = technicalReviewArgumentCaptor.getValue();
    assertThat(updatedTechnicalReview.getTechnicalReviewerWuaId())
        .isEqualTo(USER2_WEB_USER_ACCOUNT_ID.id());

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(updatedTechnicalReview.getApplicationVersion(), USER,
            TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP, REGULATOR_TECHNICAL_REVIEWER);
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenUserNotRegulatorTeam_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.empty());

    assertThat(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(USER))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenRegulatorUserButNoMembersExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(Collections.emptyList());

    assertThat(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(USER))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenRegulatorUserAndTechnicalReviewersDontExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(List.of(VIEWER_TEAM_MEMBER_VIEW, ACCESS_MANGER_TEAM_MEMBER_VIEW));

    assertThat(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(USER))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenRegulatorUserAndTechnicalReviewersExist() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(USER))
        .containsExactly(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1, TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2);
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenRegulatorUserAndTechnicalReviewersExistAndExistingTechnicalReviewer() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(technicalReviewAssignmentService.getTechnicalReviewerAssignmentCandidates(technicalReview, USER))
        .containsExactly(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2);
  }
}
