package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.SENDER_IDENTIFIER_MERGE_FIELD_NAME;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class CaseAssignmentEmailService {

  private final EmailService emailService;
  private final TeamMemberViewService teamMemberViewService;
  private final EnergyPortalUserService energyPortalUserService;

  @Autowired
  public CaseAssignmentEmailService(EmailService emailService,
                                    TeamMemberViewService teamMemberViewService,
                                    EnergyPortalUserService energyPortalUserService) {
    this.emailService = emailService;
    this.teamMemberViewService = teamMemberViewService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public void sendCaseAssignmentEmail(ApplicationVersion applicationVersion,
                                      GovukNotifyTemplate caseAssignmentEmailTemplate,
                                      FieldConsentsEmailRecipient assigneeEmailRecipient,
                                      ServiceUserDetail actionUser) {
    MergedTemplate mergedTemplate = emailService
        .getTemplateForApplication(caseAssignmentEmailTemplate, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, assigneeEmailRecipient.displayName())
        .withMailMergeField(SENDER_IDENTIFIER_MERGE_FIELD_NAME, actionUser.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        assigneeEmailRecipient,
        applicationVersion
    );
  }

  void sendCaseOwnershipReleasedEmail(ApplicationVersion applicationVersion, ServiceUserDetail caseOfficerUser) {
    var mergedTemplateBuilder = emailService
        .getTemplateForApplication(GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER, applicationVersion)
        .withMailMergeField(SENDER_IDENTIFIER_MERGE_FIELD_NAME, caseOfficerUser.displayName());

    var caseManagerEmailRecipients = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER))
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .toList();

    // iterate over the list of manager views to send an email out to each recipient
    caseManagerEmailRecipients.forEach(caseManager -> {
      var mergedTemplate = mergedTemplateBuilder
          .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, caseManager.displayName())
          .merge();

      emailService.sendEmail(
          mergedTemplate,
          caseManager,
          applicationVersion
      );
    });
  }

  void sendCaseReturnedToCaseOfficerByCamEmail(ApplicationVersion applicationVersion, ServiceUserDetail camUser) {
    var caseOfficerUser = energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationVersion.getCaseOfficerWuaId()));

    var mergedTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.CASE_RETURNED_TO_CASE_OFFICER_BY_CAM_USER, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, caseOfficerUser.displayName())
        .withMailMergeField(SENDER_IDENTIFIER_MERGE_FIELD_NAME, camUser.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        FieldConsentsEmailRecipient.from(caseOfficerUser),
        applicationVersion
    );
  }
}
