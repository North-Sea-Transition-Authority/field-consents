package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamService;

@Service
public class ConsentEmailService {

  private static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for consent issuing notification";

  private final EmailService emailService;
  private final IndustryTeamService industryTeamService;
  private final TeamMemberViewService teamMemberViewService;
  private final OrganisationUnitService organisationUnitService;
  private final EnergyPortalUserService energyPortalUserService;

  public ConsentEmailService(EmailService emailService,
                             IndustryTeamService industryTeamService,
                             TeamMemberViewService teamMemberViewService,
                             OrganisationUnitService organisationUnitService,
                             EnergyPortalUserService energyPortalUserService) {
    this.emailService = emailService;
    this.industryTeamService = industryTeamService;
    this.teamMemberViewService = teamMemberViewService;
    this.organisationUnitService = organisationUnitService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public void sendConsentIssuedEmailToOperator(ApplicationVersion applicationVersion) {
    var primaryOperator = organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE);

    var emailRecipients = getConsentIssuedEmailRecipientsForOperator(applicationVersion, primaryOperator);

    var emailMergedTemplate = emailService
        .getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, primaryOperator.name())
        .merge();

    // iterate over the list of email recipients to notify about the consent being issued
    emailRecipients.forEach(recipient ->
        emailService.sendEmail(
            emailMergedTemplate,
            recipient,
            applicationVersion
        ));
  }

  private Set<FieldConsentsEmailRecipient> getConsentIssuedEmailRecipientsForOperator(
      ApplicationVersion applicationVersion,
      OrganisationUnitWithGroupsJson primaryOperator) {
    var applicationSubmitter = FieldConsentsEmailRecipient.from(
        energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationVersion.getSubmittedByWuaId()))
    );

    var emailRecipients = new HashSet<FieldConsentsEmailRecipient>();
    emailRecipients.add(applicationSubmitter);

    primaryOperator.organisationGroups().forEach(organisationGroupDto -> {
      var teamOptional = industryTeamService.getTeamByOrganisationGroupId(organisationGroupDto.getOrganisationGroupId());

      if (teamOptional.isPresent()) {
        var teamConsentRecipients = teamMemberViewService
            .getTeamMemberViewsWithRolesForTeam(
                teamOptional.get(),
                Set.of(IndustryTeamRole.CONSENT_RECIPIENT))
            .stream()
            .map(FieldConsentsEmailRecipient::from)
            .toList();

        emailRecipients.addAll(teamConsentRecipients);
      }
    });
    return emailRecipients;
  }
}
