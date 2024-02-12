package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class CaseAssignmentEmailService {

  private final EmailService emailService;
  private final TeamMemberViewService teamMemberViewService;

  @Autowired
  public CaseAssignmentEmailService(EmailService emailService, TeamMemberViewService teamMemberViewService) {
    this.emailService = emailService;
    this.teamMemberViewService = teamMemberViewService;
  }

  public void sendCaseAssignmentEmail(ApplicationVersion applicationVersion,
                                      ServiceUserDetail caseOfficerUser,
                                      ServiceUserDetail actionUser) {

    MergedTemplate mergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER, applicationVersion)
        .withMailMergeField("CASE_OFFICER", caseOfficerUser.displayName())
        .withMailMergeField("CASE_ASSIGNEE", actionUser.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        caseOfficerUser,
        applicationVersion
    );
  }

  void sendCaseOwnershipReleasedEmail(ApplicationVersion applicationVersion, ServiceUserDetail caseOfficerUser) {
    var mergedTemplateBuilder = emailService
        .getTemplate(GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER, applicationVersion)
        .withMailMergeField("CASE_OFFICER", caseOfficerUser.displayName());

    var caseManagerTeamMemberViews = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER));

    // iterate over the list of manager views to send an email out to each recipient
    caseManagerTeamMemberViews.forEach(caseManager -> {
      MergedTemplate mergedTemplate = mergedTemplateBuilder
          .withMailMergeField("CASE_MANAGER", caseManager.getDisplayName())
          .merge();

      emailService.sendEmail(
          mergedTemplate,
          caseManager,
          applicationVersion
      );
    });
  }
}
