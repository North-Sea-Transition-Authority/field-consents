package uk.co.nstauthority.fieldconsents.application.submission;

import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class ApplicationSubmissionEmailService {

  private static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for application submission notification";

  private final EmailService emailService;
  private final TeamMemberViewService teamMemberViewService;
  private final OrganisationUnitService organisationUnitService;

  public ApplicationSubmissionEmailService(EmailService emailService,
                                           TeamMemberViewService teamMemberViewService,
                                           OrganisationUnitService organisationUnitService) {
    this.emailService = emailService;
    this.teamMemberViewService = teamMemberViewService;
    this.organisationUnitService = organisationUnitService;
  }

  public void sendNonAceApplicationSubmissionEmail(ApplicationVersion applicationVersion) {
    var primaryOperator = organisationUnitService.getOrganisationUnitByIdOrFallback(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE,
        applicationVersion.getCachedPrimaryOperatorName());

    var emailMergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.NON_ACE_APPLICATION_SUBMISSION, applicationVersion)
        .withMailMergeField("PRIMARY_OPERATOR_NAME", primaryOperator.name())
        .merge();

    var emailRecipients = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(
            TeamType.REGULATOR,
            Set.of(RegulatorTeamRole.CASE_OFFICER, RegulatorTeamRole.CASE_MANAGER))
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .toList();

    // iterate over the list of email recipients to notify about the non-ACE application submission
    emailRecipients.forEach(recipient ->
        emailService.sendEmail(
            emailMergedTemplate,
            recipient,
            applicationVersion
        ));
  }
}
