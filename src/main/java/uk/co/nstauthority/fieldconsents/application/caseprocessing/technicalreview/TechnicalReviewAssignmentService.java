package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEWER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.TECHNICAL_REVIEW_REQUEST;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Service
public class TechnicalReviewAssignmentService {

  static final UnaryOperator<String> USER_NOT_IN_TECHNICAL_REVIEWER_ROLE =
      "Cannot assign technical reviewer as user with wua id %s is not in a regulator technical reviewer role"::formatted;

  private final TechnicalReviewRepository technicalReviewRepository;
  private final RegulatorTeamService regulatorTeamService;
  private final TeamMemberViewService teamMemberViewService;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;
  private final EnergyPortalUserService energyPortalUserService;
  private final TeamService teamService;

  public TechnicalReviewAssignmentService(TechnicalReviewRepository technicalReviewRepository,
                                          RegulatorTeamService regulatorTeamService,
                                          TeamMemberViewService teamMemberViewService,
                                          ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
                                          EnergyPortalUserService energyPortalUserService,
                                          TeamService teamService) {
    this.technicalReviewRepository = technicalReviewRepository;
    this.regulatorTeamService = regulatorTeamService;
    this.teamMemberViewService = teamMemberViewService;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.energyPortalUserService = energyPortalUserService;
    this.teamService = teamService;
  }

  @Transactional
  public void assignTechnicalReviewer(TechnicalReview technicalReview,
                                      ServiceUserDetail technicalReviewerUser,
                                      ServiceUserDetail actionUser) {
    if (!regulatorTeamService.isTechnicalReviewer(WebUserAccountId.from(technicalReviewerUser))) {
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
  }

  public List<TeamMemberView> getTechnicalReviewerAssignmentCandidates(ServiceUserDetail user) {

    return regulatorTeamService.getRegulatorTeamForUser(user)
        .map(team -> teamMemberViewService.getTeamMemberViewsForTeam(team)
            .stream()
            .filter(teamMemberView -> teamMemberView.teamRoles().contains(RegulatorTeamRole.TECHNICAL_REVIEWER))
            .toList()
        )
        .orElse(Collections.emptyList())
        .stream()
        .sorted(Comparator.comparing(TeamMemberView::getDisplayName))
        .toList();
  }

  public List<TeamMemberView> getTechnicalReviewerAssignmentCandidates(TechnicalReview technicalReview,
                                                                       ServiceUserDetail user) {

    return getTechnicalReviewerAssignmentCandidates(user)
        .stream()
        .filter(teamMemberView -> !technicalReview.getTechnicalReviewerWuaId().equals(teamMemberView.wuaId().id()))
        .toList();
  }

  public List<EnergyPortalUserDto> getCurrentTechnicalReviewers() {
    // The list of current technical reviewers is the union of all current technical reviewers in the NSTA team
    // and the technical reviewers assigned to current open technical reviews.

    var teamTechnicalReviewerWuaIds = teamService.getWuaIdsOfTeamMembersWithRoles(
        TeamType.REGULATOR,
        Set.of(RegulatorTeamRole.TECHNICAL_REVIEWER)
    );
    var technicalReviewerAssignedWuaIds = technicalReviewRepository
        .findAllTechnicalReviewerWuaIdsByTechnicalReviewStatus(TechnicalReviewStatus.OPEN)
        .stream()
        .map(WebUserAccountId::from)
        .toList();

    Set<WebUserAccountId> currentTechnicalReviewersWuaIds = new HashSet<>();
    currentTechnicalReviewersWuaIds.addAll(teamTechnicalReviewerWuaIds);
    currentTechnicalReviewersWuaIds.addAll(technicalReviewerAssignedWuaIds);

    return energyPortalUserService.findByWuaIds(currentTechnicalReviewersWuaIds);
  }
}
