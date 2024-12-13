package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_TAKE_OWNERSHIP;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@Service
public class CaseAssignmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CaseAssignmentService.class);

  static final UnaryOperator<String> USER_NOT_IN_CASE_OFFICER_ROLE =
      "Cannot assign case officer as user with wua id %s is not in a regulator case officer role"::formatted;

  private final ApplicationVersionRepository applicationVersionRepository;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;
  private final EnergyPortalUserService energyPortalUserService;
  private final CaseAssignmentEmailService caseAssignmentEmailService;
  private final TeamQueryService teamQueryService;

  CaseAssignmentService(
      ApplicationVersionRepository applicationVersionRepository,
      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
      EnergyPortalUserService energyPortalUserService,
      CaseAssignmentEmailService caseAssignmentEmailService,
      TeamQueryService teamQueryService
  ) {
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.energyPortalUserService = energyPortalUserService;
    this.caseAssignmentEmailService = caseAssignmentEmailService;
    this.teamQueryService = teamQueryService;
  }

  @Transactional
  public void assignCaseOfficer(
      ApplicationVersion applicationVersion,
      ServiceUserDetail caseOfficerUser,
      ServiceUserDetail actionUser
  ) {
    if (!teamQueryService.userHasStaticRole(caseOfficerUser, TeamType.REGULATOR, Role.CASE_OFFICER)) {
      throw new IllegalArgumentException(
          USER_NOT_IN_CASE_OFFICER_ROLE.apply(String.valueOf(caseOfficerUser.wuaId())));
    }

    applicationVersion.setCaseOfficerWuaId(caseOfficerUser.wuaId());
    applicationVersion.setCurrentCaseOwner(Role.CASE_OFFICER);
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
        caseAssignmentEmailService.sendCaseAssignmentEmail(
            applicationVersion,
            GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER,
            FieldConsentsEmailRecipient.from(caseOfficerUser),
            actionUser);
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

    try {
      caseAssignmentEmailService.sendCaseOwnershipReleasedEmail(applicationVersion, user);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a case ownership released notification by case officer with wuaId {} \
              for application version with id {} failed. \
              Note: this hasn't prevented the ownership of the case to be released.""",
          user.wuaId(), applicationVersion.getId(), exception);
    }
  }

  public boolean isCaseOfficerAssigned(ApplicationVersion applicationVersion) {
    return Objects.nonNull(applicationVersion.getCaseOfficerWuaId())
        && Role.CASE_OFFICER.equals(applicationVersion.getCurrentCaseOwner());
  }

  public boolean isCamAssigned(ApplicationVersion applicationVersion) {
    return Objects.nonNull(applicationVersion.getCamWuaId())
        && Role.CONSENTS_AND_AUTHORISATIONS_MANAGER.equals(applicationVersion.getCurrentCaseOwner());
  }

  public List<TeamMemberView> getCaseOfficers() {
    var caseOfficerTeamRoles = teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.CASE_OFFICER)
        .toList();

    if (caseOfficerTeamRoles.isEmpty()) {
      return List.of();
    }

    return teamQueryService.getTeamMemberViews(caseOfficerTeamRoles);
  }

  /**
   * The list of current case officers is the union of all current case officers in the NSTA team
   * and the case officers assigned to current applications.
   */
  public List<EnergyPortalUserDto> getCurrentCaseOfficers() {
    var currentCaseOfficersWuaIds = new HashSet<>(getActiveCaseOfficerWebUserAccountIds());

    applicationVersionRepository
        .findAllCaseOfficerWuaIdsByApplicationVersionStatus(ApplicationVersionStatus.SUBMITTED)
        .stream()
        .map(WebUserAccountId::from)
        .forEach(currentCaseOfficersWuaIds::add);

    return energyPortalUserService.findByWuaIds(currentCaseOfficersWuaIds);
  }

  public List<EnergyPortalUserDto> getActiveCaseOfficers() {
    var activeCaseOfficers = getActiveCaseOfficerWebUserAccountIds();
    return energyPortalUserService.findByWuaIds(activeCaseOfficers);
  }

  private List<WebUserAccountId> getActiveCaseOfficerWebUserAccountIds() {
    return teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.CASE_OFFICER)
        .map(TeamRole::getWuaId)
        .map(WebUserAccountId::from)
        .toList();
  }

  @Transactional
  public void returnToCaseOfficer(ApplicationVersion applicationVersion,
                                  ServiceUserDetail actionUser) {
    applicationVersion.setCamWuaId(null);
    applicationVersion.setCurrentCaseOwner(Role.CASE_OFFICER);
    applicationVersionRepository.save(applicationVersion);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        actionUser,
        CASE_OFFICER_ASSIGN_OWNERSHIP,
        ApplicationWorkAreaPriorityGroup.REGULATOR
    );

    try {
      caseAssignmentEmailService.sendCaseReturnedToCaseOfficerByCamEmail(applicationVersion, actionUser);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a case returned to case officer notification by cam user with wuaId {} \
              for application version with id {} failed. \
              Note: this hasn't prevented the case to be returned to the case officer.""",
          actionUser.wuaId(), applicationVersion.getId(), exception);
    }
  }

  public Optional<WebUserAccountId> findCaseOfficerWuaId(ApplicationVersion applicationVersion) {
    return Optional.ofNullable(applicationVersion.getCaseOfficerWuaId())
        .map(WebUserAccountId::from);
  }
}
