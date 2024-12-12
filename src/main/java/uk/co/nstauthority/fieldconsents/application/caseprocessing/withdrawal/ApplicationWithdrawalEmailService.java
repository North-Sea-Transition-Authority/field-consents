package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipientService;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class ApplicationWithdrawalEmailService {

  private static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for application withdrawal notification";
  static final String CASE_MANAGERS_RECIPIENT_DISPLAY_NAME = "Case Managers";

  private final EmailService emailService;
  private final EnergyPortalUserService energyPortalUserService;
  private final OrganisationUnitService organisationUnitService;
  private final FieldConsentsEmailRecipientService fieldConsentsEmailRecipientService;
  private final TeamQueryService teamQueryService;

  ApplicationWithdrawalEmailService(
      EmailService emailService,
      EnergyPortalUserService energyPortalUserService,
      OrganisationUnitService organisationUnitService,
      FieldConsentsEmailRecipientService fieldConsentsEmailRecipientService,
      TeamQueryService teamQueryService
  ) {
    this.emailService = emailService;
    this.energyPortalUserService = energyPortalUserService;
    this.organisationUnitService = organisationUnitService;
    this.fieldConsentsEmailRecipientService = fieldConsentsEmailRecipientService;
    this.teamQueryService = teamQueryService;
  }

  public void sendApplicationWithdrawalRequestEmail(ApplicationWithdrawal applicationWithdrawal) {
    var applicationVersion = applicationWithdrawal.getApplicationVersion();

    var primaryOperator = organisationUnitService.getOrganisationUnitByIdOrFallback(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE,
        applicationVersion.getCachedPrimaryOperatorName()
    );

    var mergedTemplateBuilder = emailService
        .getTemplateForApplication(GovukNotifyTemplate.APPLICATION_WITHDRAWAL_REQUEST, applicationVersion)
        .withMailMergeField("PRIMARY_OPERATOR_NAME", primaryOperator.name());

    // email the case officer if available
    if (applicationVersion.getCaseOfficerWuaId() != null) {
      var caseOfficerEmailRecipient = FieldConsentsEmailRecipient.from(
          energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationVersion.getCaseOfficerWuaId()))
      );

      var mergedTemplate = mergedTemplateBuilder
          .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, caseOfficerEmailRecipient.displayName())
          .merge();

      emailService.sendEmail(
          mergedTemplate,
          caseOfficerEmailRecipient,
          applicationVersion
      );

      return;
    }

    var caseManagerTeamRoles = teamQueryService.getTeamRoles(TeamType.REGULATOR)
        .stream()
        .filter(teamRole -> teamRole.getRole() == Role.CASE_MANAGER)
        .toList();

    var mergedTemplate = mergedTemplateBuilder
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME)
        .merge();

    // otherwise email all case managers
    teamQueryService.getTeamMemberViews(caseManagerTeamRoles)
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .forEach(emailRecipient -> emailService.sendEmail(mergedTemplate, emailRecipient, applicationVersion));
  }

  public void sendApplicationWithdrawalResponseEmail(ApplicationWithdrawal applicationWithdrawal) {
    var applicationVersion = applicationWithdrawal.getApplicationVersion();
    var withdrawalRequesterEmailRecipient = FieldConsentsEmailRecipient.from(
        energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationWithdrawal.getRequestedByWuaId()))
    );

    var organisationUnitWithGroupsJson = organisationUnitService
        .getOrganisationUnitWithGroupsById(applicationVersion.getPrimaryOperatorOuId(), ORGANISATION_LOOKUP_PURPOSE);

    var distinctEmailRecipients = fieldConsentsEmailRecipientService.getDistinctEmailRecipientsWithRoles(
        organisationUnitWithGroupsJson, RoleGroup.INDUSTRY_EDIT_APPLICATION_ROLES);

    distinctEmailRecipients.add(withdrawalRequesterEmailRecipient);

    var templateBuilder = emailService
        .getTemplateForApplication(GovukNotifyTemplate.APPLICATION_WITHDRAWAL_RESPONSE, applicationVersion);

    // iterate over the list of operator recipients to notify about the outcome of the withdrawal request
    distinctEmailRecipients.forEach(recipient -> {
      templateBuilder
          .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, recipient.displayName());

      emailService.sendEmail(
          templateBuilder.merge(),
          recipient,
          applicationVersion
      );
    });
  }
}
