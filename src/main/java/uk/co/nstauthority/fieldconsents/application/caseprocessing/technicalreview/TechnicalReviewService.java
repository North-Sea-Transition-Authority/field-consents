package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus.OPEN;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Service
public class TechnicalReviewService {

  static final UnaryOperator<String> OPEN_TECHNICAL_REVIEW_EXISTS =
      "A technical review is already open for the application with version id %s"::formatted;

  private final Clock clock;

  private final TechnicalReviewRepository technicalReviewRepository;

  private final RegulatorTeamService regulatorTeamService;

  private final TeamMemberViewService teamMemberViewService;

  public TechnicalReviewService(Clock clock,
                                TechnicalReviewRepository technicalReviewRepository,
                                RegulatorTeamService regulatorTeamService,
                                TeamMemberViewService teamMemberViewService) {
    this.clock = clock;
    this.technicalReviewRepository = technicalReviewRepository;
    this.regulatorTeamService = regulatorTeamService;
    this.teamMemberViewService = teamMemberViewService;
  }

  public List<TeamMemberView> getTechnicalReviewerAssignmentCandidates(ApplicationVersion applicationVersion,
                                                                       ServiceUserDetail user) {

    var technicalReviewOptional = findOpenTechnicalReview(applicationVersion);

    return regulatorTeamService.getRegulatorTeamForUser(user)
        .map(team -> teamMemberViewService.getTeamMemberViewsForTeam(team)
            .stream()
            .filter(teamMemberView -> teamMemberView.teamRoles().contains(RegulatorTeamRole.TECHNICAL_REVIEWER))
            .filter(teamMemberView -> technicalReviewOptional.isEmpty()
                    || !technicalReviewOptional.get().getTechnicalReviewerWuaId().equals(teamMemberView.wuaId().id()))
            .toList()
        )
        .orElse(Collections.emptyList())
        .stream()
        .sorted(Comparator.comparing(TeamMemberView::getDisplayName))
        .toList();
  }

  public boolean openTechnicalReviewExists(ApplicationVersion applicationVersion) {
    return technicalReviewRepository
        .existsByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN);
  }

  public Optional<TechnicalReview> findOpenTechnicalReview(ApplicationVersion applicationVersion) {
    return technicalReviewRepository.findByApplicationVersionAndTechnicalReviewStatus(applicationVersion, OPEN);
  }

  public TechnicalReviewRequestForm getTechnicalReviewRequestForm(ApplicationVersion applicationVersion) {
    if (openTechnicalReviewExists(applicationVersion)) {
      throw new IllegalStateException(OPEN_TECHNICAL_REVIEW_EXISTS.apply(String.valueOf(applicationVersion.getId())));
    }
    return new TechnicalReviewRequestForm();
  }

  @Transactional
  public void saveTechnicalReviewRequest(ApplicationVersion applicationVersion,
                                         Instant deadlineInstant,
                                         String requestText,
                                         ServiceUserDetail technicalReviewerUser,
                                         ServiceUserDetail user) {
    var technicalReview = new TechnicalReview();
    technicalReview.setApplicationVersion(applicationVersion);
    technicalReview.setTechnicalReviewStatus(OPEN);
    technicalReview.setTechnicalReviewerWuaId(technicalReviewerUser.wuaId());
    technicalReview.setRequestedByWuaId(user.wuaId());
    technicalReview.setRequestedDateTime(clock.instant());
    technicalReview.setRequestText(requestText);
    technicalReview.setDeadlineDateTime(deadlineInstant);
    technicalReviewRepository.save(technicalReview);
    // TODO - FCS-344 and FCS-80: add call to prioritise in work area for a new REGULATOR_TECHNICAL_REVIEWER group
    //applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(applicationVersion, user,
    //    TECHNICAL_REVIEW_STARTED, REGULATOR_TECHNICAL_REVIEWER);
  }
}
