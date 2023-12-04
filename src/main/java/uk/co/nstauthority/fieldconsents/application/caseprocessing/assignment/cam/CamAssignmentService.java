package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.cam;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CAM_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Service
public class CamAssignmentService {

  static final UnaryOperator<String> USER_NOT_IN_CAM_ROLE =
      "Cannot assign CAM. User with wua id %s is not in a regulator consents and authorisations manager role"::formatted;

  private final ApplicationVersionRepository applicationVersionRepository;
  private final RegulatorTeamService regulatorTeamService;
  private final TeamMemberViewService teamMemberViewService;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  public CamAssignmentService(ApplicationVersionRepository applicationVersionRepository,
                              RegulatorTeamService regulatorTeamService, TeamMemberViewService teamMemberViewService,
                              ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService) {
    this.applicationVersionRepository = applicationVersionRepository;
    this.regulatorTeamService = regulatorTeamService;
    this.teamMemberViewService = teamMemberViewService;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
  }

  @Transactional
  public void assignCamUser(ApplicationVersion applicationVersion,
                            ServiceUserDetail camUser,
                            ServiceUserDetail actionUser) {
    if (!regulatorTeamService.isCamUser(WebUserAccountId.from(camUser))) {
      throw new IllegalArgumentException(
          USER_NOT_IN_CAM_ROLE.apply(String.valueOf(camUser.wuaId())));
    }
    applicationVersion.setCamWuaId(camUser.wuaId());
    applicationVersion.setCurrentCaseOwner(RegulatorTeamRole.CONSENTS_AND_AUTHORISATIONS_MANAGER);
    applicationVersionRepository.save(applicationVersion);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        actionUser,
        CAM_ASSIGN_OWNERSHIP,
        ApplicationWorkAreaPriorityGroup.REGULATOR_CAM
    );
  }

  public List<TeamMemberView> getCamUserAssignmentCandidates(ServiceUserDetail user) {
    return regulatorTeamService.getRegulatorTeamForUser(user)
        .map(team -> teamMemberViewService.getTeamMemberViewsForTeam(team)
            .stream()
            .filter(teamMemberView -> teamMemberView.teamRoles().contains(CONSENTS_AND_AUTHORISATIONS_MANAGER))
            .toList()
        )
        .orElse(Collections.emptyList())
        .stream()
        .sorted(Comparator.comparing(TeamMemberView::getDisplayName))
        .toList();
  }
}
