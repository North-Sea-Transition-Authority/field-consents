package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.SENDER_IDENTIFIER_MERGE_FIELD_NAME;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class CaseAssignmentEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CaseAssignmentEmailService.class);

  private final EmailService emailService;
  private final EnergyPortalUserService energyPortalUserService;
  private final TeamQueryService teamQueryService;

  CaseAssignmentEmailService(
      EmailService emailService,
      EnergyPortalUserService energyPortalUserService,
      TeamQueryService teamQueryService
  ) {
    this.emailService = emailService;
    this.energyPortalUserService = energyPortalUserService;
    this.teamQueryService = teamQueryService;
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
    var caseManagerTeamRoles = teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.CASE_MANAGER)
        .toList();

    var emailTemplate = GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER;

    if (caseManagerTeamRoles.isEmpty()) {
      LOGGER.info("Didn't find any case managers to send [{}] email to", emailTemplate);
      return;
    }

    var mergedTemplateBuilder = emailService
        .getTemplateForApplication(emailTemplate, applicationVersion)
        .withMailMergeField(SENDER_IDENTIFIER_MERGE_FIELD_NAME, caseOfficerUser.displayName());

    teamQueryService.getTeamMemberViews(caseManagerTeamRoles)
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .forEach(emailRecipient -> {
          var mergedTemplate = mergedTemplateBuilder
              .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, emailRecipient.displayName())
              .merge();

          emailService.sendEmail(
              mergedTemplate,
              emailRecipient,
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
