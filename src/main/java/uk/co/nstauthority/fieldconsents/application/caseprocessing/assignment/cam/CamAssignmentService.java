package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CAM_ASSIGN_OWNERSHIP;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.CaseAssignmentEmailService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@Service
public class CamAssignmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CamAssignmentService.class);

  static final UnaryOperator<String> USER_NOT_IN_CAM_ROLE =
      "Cannot assign CAM. User with wua id %s is not in a regulator consents and authorisations manager role"::formatted;

  private final ApplicationVersionRepository applicationVersionRepository;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;
  private final CaseAssignmentEmailService caseAssignmentEmailService;
  private final TeamQueryService teamQueryService;

  CamAssignmentService(
      ApplicationVersionRepository applicationVersionRepository,
      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
      CaseAssignmentEmailService caseAssignmentEmailService,
      TeamQueryService teamQueryService
  ) {
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.caseAssignmentEmailService = caseAssignmentEmailService;
    this.teamQueryService = teamQueryService;
  }

  @Transactional
  public void assignCamUser(ApplicationVersion applicationVersion,
                            ServiceUserDetail camUser,
                            ServiceUserDetail actionUser) {
    if (!teamQueryService.userHasStaticRole(camUser, TeamType.REGULATOR, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)) {
      throw new IllegalArgumentException(
          USER_NOT_IN_CAM_ROLE.apply(String.valueOf(camUser.wuaId())));
    }

    applicationVersion.setCamWuaId(camUser.wuaId());
    applicationVersion.setCurrentCaseOwner(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER);
    applicationVersionRepository.save(applicationVersion);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        actionUser,
        CAM_ASSIGN_OWNERSHIP,
        ApplicationWorkAreaPriorityGroup.REGULATOR
    );

    try {
      caseAssignmentEmailService.sendCaseAssignmentEmail(
          applicationVersion,
          GovukNotifyTemplate.CASE_ASSIGNED_TO_CAM_USER,
          FieldConsentsEmailRecipient.from(camUser),
          actionUser);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a case assignment notification to cam user with wuaId {} for application version \
              with id {} failed. Note: this hasn't prevented the assignment of the case to the cam user.""",
          camUser.wuaId(), applicationVersion.getId(), exception);
    }
  }

  public List<TeamMemberView> getCamUserAssignmentCandidates(ApplicationVersion applicationVersion) {
    var camTeamRoles = teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
        // exclude the currently assigned cam user if one is assigned
        .filter(teamRole -> applicationVersion.getCamWuaId() == null
            || !applicationVersion.getCamWuaId().equals(teamRole.getWuaId()))
        .toList();

    if (camTeamRoles.isEmpty()) {
      return List.of();
    }

    return teamQueryService.getTeamMemberViews(camTeamRoles);
  }

  public Optional<WebUserAccountId> findCamWuaId(ApplicationVersion applicationVersion) {
    return Optional.ofNullable(applicationVersion.getCamWuaId())
        .map(WebUserAccountId::from);
  }
}
