package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ACCESS_MANGER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.SERVICE_USER_DETAIL_USER_5;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TEAM_MEMBER_VIEW_LIST;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.VIEWER_TEAM_MEMBER_VIEW;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService.OPEN_TECHNICAL_REVIEW_EXISTS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.DEADLINE_AHEAD_HOURS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.REGULATOR_TEAM;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEW_REQUEST_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.USER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEW_REQUEST;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewServiceTest {

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
  private TechnicalReviewService technicalReviewService;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReview;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    technicalReview = TechnicalReviewTestUtil
        .getOpenTechnicalReview(applicationVersion, SERVICE_USER_DETAIL_USER_5, clock);
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenUserNotRegulatorTeam_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.empty());

    assertThat(technicalReviewService.getTechnicalReviewerAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenRegulatorUserAndTechnicalReviewersExist() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(technicalReviewService.getTechnicalReviewerAssignmentCandidates(applicationVersion, USER))
        .containsExactly(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1, TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2);
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenRegulatorUserAndTechnicalReviewersExistAndExistingTechnicalReviewer() {
    when(technicalReviewRepository.findByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN))
        .thenReturn(Optional.of(technicalReview));
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(TEAM_MEMBER_VIEW_LIST);

    assertThat(technicalReviewService.getTechnicalReviewerAssignmentCandidates(applicationVersion, USER))
        .containsExactly(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2);
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenRegulatorUserButNoMembersExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(Collections.emptyList());

    assertThat(technicalReviewService.getTechnicalReviewerAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewerAssignmentCandidates_whenRegulatorUserAndTechnicalReviewersDontExist_thenEmpty() {
    when(regulatorTeamService.getRegulatorTeamForUser(USER))
        .thenReturn(Optional.of(REGULATOR_TEAM));
    when(teamMemberViewService.getTeamMemberViewsForTeam(REGULATOR_TEAM))
        .thenReturn(List.of(VIEWER_TEAM_MEMBER_VIEW, ACCESS_MANGER_TEAM_MEMBER_VIEW));

    assertThat(technicalReviewService.getTechnicalReviewerAssignmentCandidates(applicationVersion, USER))
        .isEmpty();
  }

  @Test
  void openTechnicalReviewExists_whenExists() {
    when(technicalReviewRepository
        .existsByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN))
        .thenReturn(true);

    assertTrue(technicalReviewService.openTechnicalReviewExists(applicationVersion));
  }

  @Test
  void openWithdrawalExists_whenDoesNotExist() {
    when(technicalReviewRepository
        .existsByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN))
        .thenReturn(false);

    assertFalse(technicalReviewService.openTechnicalReviewExists(applicationVersion));
  }

  @Test
  void findOpenTechnicalReview_whenExists() {
    when(technicalReviewRepository
        .findByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN))
        .thenReturn(Optional.of(technicalReview));

    assertThat(technicalReviewService.findOpenTechnicalReview(applicationVersion))
        .contains(technicalReview);
  }

  @Test
  void findOpenTechnicalReview_whenDoesNotExist() {
    when(technicalReviewRepository
        .findByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN))
        .thenReturn(Optional.empty());

    assertThat(technicalReviewService.findOpenTechnicalReview(applicationVersion))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewRequestForm_noOpenTechnicalReviewExists() {
    when(technicalReviewRepository.existsByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN))
        .thenReturn(false);

    assertThat(technicalReviewService.getTechnicalReviewRequestForm(applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(new TechnicalReviewRequestForm());
  }

  @Test
  void getTechnicalReviewRequestForm_technicalReviewAlreadyOpen() {
    when(technicalReviewRepository.existsByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN))
        .thenReturn(true);

    assertThatThrownBy(() -> technicalReviewService.getTechnicalReviewRequestForm(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId())));
  }

  @Test
  void saveTechnicalReviewRequest() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    technicalReviewService.saveTechnicalReviewRequest(applicationVersion,
        clock.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS),
        TECHNICAL_REVIEW_REQUEST_TEXT,
        SERVICE_USER_DETAIL_USER_5,
        USER
    );

    ArgumentCaptor<TechnicalReview> technicalReviewArgumentCaptor = ArgumentCaptor.forClass(TechnicalReview.class);
    verify(technicalReviewRepository, times(1)).save(technicalReviewArgumentCaptor.capture());
    var actualTechnicalReview = technicalReviewArgumentCaptor.getValue();

    assertThat(actualTechnicalReview)
        .usingRecursiveComparison()
        .isEqualTo(technicalReview);

    verify(applicationWorkAreaPriorityService, times(1))
        .prioritiseApplicationInWorkArea(applicationVersion, USER, TECHNICAL_REVIEW_REQUEST, REGULATOR_TECHNICAL_REVIEWER);
  }
}
