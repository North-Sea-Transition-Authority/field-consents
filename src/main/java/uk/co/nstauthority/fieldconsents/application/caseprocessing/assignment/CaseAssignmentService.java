package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_ASSIGN_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_RELEASE_OWNERSHIP;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CASE_OFFICER_TAKE_OWNERSHIP;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamService;

@Service
public class CaseAssignmentService {

  private final ApplicationVersionRepository applicationVersionRepository;

  private final RegulatorTeamService regulatorTeamService;

  private final TeamMemberViewService teamMemberViewService;

  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Autowired
  public CaseAssignmentService(ApplicationVersionRepository applicationVersionRepository,
                               RegulatorTeamService regulatorTeamService,
                               TeamMemberViewService teamMemberViewService,
                               ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService) {
    this.applicationVersionRepository = applicationVersionRepository;
    this.regulatorTeamService = regulatorTeamService;
    this.teamMemberViewService = teamMemberViewService;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
  }

  @Transactional
  public void assignCaseOfficer(ApplicationVersion applicationVersion,
                                ServiceUserDetail caseOfficerUser,
                                ServiceUserDetail actionUser) {
    if (!regulatorTeamService.isCaseOfficer(WebUserAccountId.from(caseOfficerUser))) {
      throw new IllegalStateException(
          "Cannot assign case officer as user with wua id %s is not in a regulator case officer role"
              .formatted(caseOfficerUser.wuaId()));
    }
    applicationVersion.setCaseOfficerWuaId(caseOfficerUser.wuaId());
    applicationVersionRepository.save(applicationVersion);

    // figure out the work area priority reason, if the person making the assignment is the same as the assignee
    // then we must be taking ownership, otherwise a case officer is being assigned by another user
    var applicationWorkAreaPriorityReason = actionUser.equals(caseOfficerUser)
        ? CASE_OFFICER_TAKE_OWNERSHIP
        : CASE_OFFICER_ASSIGN_OWNERSHIP;

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        actionUser,
        applicationWorkAreaPriorityReason,
        REGULATOR
    );
  }

  @Transactional
  public void unassignCaseOfficer(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    applicationVersion.setCaseOfficerWuaId(null);
    applicationVersionRepository.save(applicationVersion);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, CASE_OFFICER_RELEASE_OWNERSHIP, REGULATOR);
  }

  public List<TeamMemberView> getCaseOfficerAssignmentCandidates(ApplicationVersion applicationVersion,
                                                                 ServiceUserDetail user) {
    return regulatorTeamService.getRegulatorTeamForUser(user)
        .map(team -> teamMemberViewService.getTeamMemberViewsForTeam(team)
            .stream()
            .filter(teamMemberView -> teamMemberView.teamRoles().contains(RegulatorTeamRole.CASE_OFFICER)
                && (
                    Objects.isNull(applicationVersion.getCaseOfficerWuaId())
                        || !applicationVersion.getCaseOfficerWuaId().equals(teamMemberView.wuaId().id())
                )
            )
            .toList())
        .orElse(Collections.emptyList())
        .stream()
        .sorted(Comparator.comparing(TeamMemberView::getDisplayName))
        .toList();
  }
}
