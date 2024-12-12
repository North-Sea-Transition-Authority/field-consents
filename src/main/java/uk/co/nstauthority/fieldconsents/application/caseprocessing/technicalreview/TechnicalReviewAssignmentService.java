package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEW_REQUEST;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@Service
public class TechnicalReviewAssignmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TechnicalReviewAssignmentService.class);

  static final UnaryOperator<String> USER_NOT_IN_TECHNICAL_REVIEWER_ROLE =
      "Cannot assign technical reviewer as user with wua id %s is not in a regulator technical reviewer role"::formatted;

  private final TechnicalReviewRepository technicalReviewRepository;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;
  private final EnergyPortalUserService energyPortalUserService;
  private final TechnicalReviewEmailService technicalReviewEmailService;
  private final TeamQueryService teamQueryService;

  TechnicalReviewAssignmentService(
      TechnicalReviewRepository technicalReviewRepository,
      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
      EnergyPortalUserService energyPortalUserService,
      TechnicalReviewEmailService technicalReviewEmailService,
      TeamQueryService teamQueryService
  ) {
    this.technicalReviewRepository = technicalReviewRepository;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.energyPortalUserService = energyPortalUserService;
    this.technicalReviewEmailService = technicalReviewEmailService;
    this.teamQueryService = teamQueryService;
  }

  @Transactional
  public void assignTechnicalReviewer(TechnicalReview technicalReview,
                                      ServiceUserDetail technicalReviewerUser,
                                      ServiceUserDetail actionUser) {
    if (!teamQueryService.userHasStaticRole(technicalReviewerUser, TeamType.REGULATOR, Role.TECHNICAL_REVIEWER)) {
      throw new IllegalArgumentException(
          USER_NOT_IN_TECHNICAL_REVIEWER_ROLE.apply(String.valueOf(technicalReviewerUser.wuaId())));
    }

    var priorityReason = Objects.isNull(technicalReview.getTechnicalReviewerWuaId())
        ? TECHNICAL_REVIEW_REQUEST
        : TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP;

    technicalReview.setTechnicalReviewerWuaId(technicalReviewerUser.wuaId());
    technicalReviewRepository.save(technicalReview);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        technicalReview.getRequestApplicationVersion(),
        actionUser,
        priorityReason,
        REGULATOR_TECHNICAL_REVIEWER
    );

    try {
      technicalReviewEmailService.sendTechnicalReviewRequestEmail(technicalReview, actionUser);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a technical review notification by user with wuaId [{}] for application \
              version with id [{}] failed. \
              Note: this hasn't prevented the assignment of the case to the technical reviewer.
              """,
          actionUser.wuaId(), technicalReview.getRequestApplicationVersion().getId(), exception);
    }
  }

  public List<TeamMemberView> getTechnicalReviewerAssignmentCandidates() {
    var technicalReviewerTeamRoles = teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.TECHNICAL_REVIEWER)
        .toList();

    return teamQueryService.getTeamMemberViews(technicalReviewerTeamRoles);
  }

  public List<TeamMemberView> getTechnicalReviewerAssignmentCandidates(TechnicalReview technicalReview) {

    return getTechnicalReviewerAssignmentCandidates()
        .stream()
        .filter(teamMemberView -> !technicalReview.getTechnicalReviewerWuaId().equals(teamMemberView.wuaId()))
        .toList();
  }

  public List<EnergyPortalUserDto> getCurrentTechnicalReviewers() {
    // The list of current technical reviewers is the union of all current technical reviewers in the NSTA team
    // and the technical reviewers assigned to current open technical reviews.
    var currentTechnicalReviewersWuaIds = new HashSet<WebUserAccountId>();

    teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.TECHNICAL_REVIEWER)
        .map(TeamRole::getWuaId)
        .map(WebUserAccountId::from)
        .forEach(currentTechnicalReviewersWuaIds::add);

    technicalReviewRepository
        .findAllTechnicalReviewerWuaIdsByTechnicalReviewStatus(TechnicalReviewStatus.OPEN)
        .stream()
        .map(WebUserAccountId::from)
        .forEach(currentTechnicalReviewersWuaIds::add);

    return energyPortalUserService.findByWuaIds(currentTechnicalReviewersWuaIds);
  }
}
