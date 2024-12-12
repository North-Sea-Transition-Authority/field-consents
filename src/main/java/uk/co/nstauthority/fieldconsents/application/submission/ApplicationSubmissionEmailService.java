package uk.co.nstauthority.fieldconsents.application.submission;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class ApplicationSubmissionEmailService {

  private static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for application submission notification";
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSubmissionEmailService.class);

  private final EmailService emailService;
  private final OrganisationUnitService organisationUnitService;
  private final TeamQueryService teamQueryService;

  ApplicationSubmissionEmailService(
      EmailService emailService,
      OrganisationUnitService organisationUnitService,
      TeamQueryService teamQueryService
  ) {
    this.emailService = emailService;
    this.organisationUnitService = organisationUnitService;
    this.teamQueryService = teamQueryService;
  }

  public void sendNonAceApplicationSubmissionEmail(ApplicationVersion applicationVersion) {
    var regulatorTeamRoles = teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.CASE_OFFICER || teamRole.getRole() == Role.CASE_MANAGER)
        .toList();

    var emailRecipients = teamQueryService.getTeamMemberViews(regulatorTeamRoles)
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .collect(Collectors.toSet());

    var emailTemplate = GovukNotifyTemplate.NON_ACE_APPLICATION_SUBMISSION;

    if (emailRecipients.isEmpty()) {
      LOGGER.info("Didn't find any case officers or case managers to send [{}] email to", emailTemplate);
      return;
    }

    var primaryOperator = organisationUnitService.getOrganisationUnitByIdOrFallback(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE,
        applicationVersion.getCachedPrimaryOperatorName()
    );

    var emailMergedTemplate = emailService
        .getTemplateForApplication(emailTemplate, applicationVersion)
        .withMailMergeField("PRIMARY_OPERATOR_NAME", primaryOperator.name())
        .merge();

    emailRecipients.forEach(emailRecipient -> emailService.sendEmail(emailMergedTemplate, emailRecipient, applicationVersion));
  }
}
