package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_TAKE_OWNERSHIP;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason;
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
public class CaseAssignmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CaseAssignmentService.class);

  static final UnaryOperator<String> USER_NOT_IN_CASE_OFFICER_ROLE =
      "Cannot assign case officer as user with wua id %s is not in a regulator case officer role"::formatted;

  private final ApplicationVersionRepository applicationVersionRepository;
  private final RegulatorTeamService regulatorTeamService;
  private final TeamMemberViewService teamMemberViewService;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;
  private final EnergyPortalUserService energyPortalUserService;
  private final TeamService teamService;
  private final CaseAssignmentEmailService caseAssignmentEmailService;

  @Autowired
  public CaseAssignmentService(ApplicationVersionRepository applicationVersionRepository,
                               RegulatorTeamService regulatorTeamService,
                               TeamMemberViewService teamMemberViewService,
                               ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
                               EnergyPortalUserService energyPortalUserService,
                               TeamService teamService,
                               CaseAssignmentEmailService caseAssignmentEmailService) {
    this.applicationVersionRepository = applicationVersionRepository;
    this.regulatorTeamService = regulatorTeamService;
    this.teamMemberViewService = teamMemberViewService;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.energyPortalUserService = energyPortalUserService;
    this.teamService = teamService;
    this.caseAssignmentEmailService = caseAssignmentEmailService;
  }

  @Transactional
  public void assignCaseOfficer(ApplicationVersion applicationVersion,
                                ServiceUserDetail caseOfficerUser,
                                ServiceUserDetail actionUser) {
    if (!regulatorTeamService.isCaseOfficer(WebUserAccountId.from(caseOfficerUser))) {
      throw new IllegalArgumentException(
          USER_NOT_IN_CASE_OFFICER_ROLE.apply(String.valueOf(caseOfficerUser.wuaId())));
    }
    applicationVersion.setCaseOfficerWuaId(caseOfficerUser.wuaId());
    applicationVersion.setCurrentCaseOwner(RegulatorTeamRole.CASE_OFFICER);
    applicationVersionRepository.save(applicationVersion);

    // figure out the work area priority reason, if the person making the assignment is the same as the assignee
    // then we must be taking ownership, otherwise a case officer is being assigned by another user
    var isTakeOwnership = actionUser.equals(caseOfficerUser);
    var applicationWorkAreaPriorityReason = isTakeOwnership
        ? CASE_OFFICER_TAKE_OWNERSHIP
        : CASE_OFFICER_ASSIGN_OWNERSHIP;

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        actionUser,
        applicationWorkAreaPriorityReason,
        ApplicationWorkAreaPriorityGroup.REGULATOR
    );

    // If the user assigning the case isn't the same as the assignee then send an email to the assigned case officer
    if (!isTakeOwnership) {
      try {
        caseAssignmentEmailService.sendCaseAssignmentEmail(applicationVersion, caseOfficerUser, actionUser);
      } catch (Exception exception) {
        LOGGER.error("An attempt to send a case assignment notification to case officer with wuaId {} for application version " +
                "with id {} failed. Note: this hasn't prevented the assignment of the case to the case officer.",
            caseOfficerUser.wuaId(), applicationVersion.getId(), exception);
      }
    }
  }

  @Transactional
  public void unassignCaseOfficer(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    applicationVersion.setCaseOfficerWuaId(null);
    applicationVersion.setCurrentCaseOwner(null);
    applicationVersionRepository.save(applicationVersion);
    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        user,
        ApplicationWorkAreaPriorityReason.CASE_OFFICER_RELEASE_OWNERSHIP,
        ApplicationWorkAreaPriorityGroup.REGULATOR);
  }

  public List<TeamMemberView> getCaseOfficerAssignmentCandidates(ApplicationVersion applicationVersion,
                                                                 ServiceUserDetail user) {
    return regulatorTeamService.getRegulatorTeamForUser(user)
        .map(team -> teamMemberViewService.getTeamMemberViewsForTeam(team)
            .stream()
            .filter(teamMemberView -> teamMemberView.teamRoles().contains(RegulatorTeamRole.CASE_OFFICER))
            .filter(teamMemberView -> Objects.isNull(applicationVersion.getCaseOfficerWuaId())
                    || !applicationVersion.getCaseOfficerWuaId().equals(teamMemberView.wuaId().id()))
            .toList()
        )
        .orElse(Collections.emptyList())
        .stream()
        .sorted(Comparator.comparing(TeamMemberView::getDisplayName))
        .toList();
  }

  /**
   * The list of current case officers is the union of all current case officers in the NSTA team
   * and the case officers assigned to current applications.
   */
  public List<EnergyPortalUserDto> getCurrentCaseOfficers() {
    var caseOfficerAssignedWuaIds = applicationVersionRepository
        .findAllCaseOfficerWuaIdsByApplicationVersionStatus(ApplicationVersionStatus.SUBMITTED)
        .stream()
        .map(WebUserAccountId::from)
        .toList();

    var currentCaseOfficersWuaIds = new HashSet<WebUserAccountId>();

    currentCaseOfficersWuaIds.addAll(getActiveCaseOfficerWebUserAccountIds());
    currentCaseOfficersWuaIds.addAll(caseOfficerAssignedWuaIds);

    return energyPortalUserService.findByWuaIds(currentCaseOfficersWuaIds);
  }

  public List<EnergyPortalUserDto> getActiveCaseOfficers() {
    var activeCaseOfficers = getActiveCaseOfficerWebUserAccountIds();
    return energyPortalUserService.findByWuaIds(activeCaseOfficers);
  }

  private List<WebUserAccountId> getActiveCaseOfficerWebUserAccountIds() {
    return teamService.getWuaIdsOfTeamMembersWithRoles(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_OFFICER));
  }

  @Transactional
  public void returnToCaseOfficer(ApplicationVersion applicationVersion,
                                  ServiceUserDetail actionUser) {
    applicationVersion.setCamWuaId(null);
    applicationVersion.setCurrentCaseOwner(RegulatorTeamRole.CASE_OFFICER);
    applicationVersionRepository.save(applicationVersion);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        actionUser,
        CASE_OFFICER_ASSIGN_OWNERSHIP,
        ApplicationWorkAreaPriorityGroup.REGULATOR
    );
  }

  public Optional<WebUserAccountId> findCaseOfficerWuaId(ApplicationVersion applicationVersion) {
    return Optional.ofNullable(applicationVersion.getCaseOfficerWuaId())
        .map(WebUserAccountId::from);
  }
}
