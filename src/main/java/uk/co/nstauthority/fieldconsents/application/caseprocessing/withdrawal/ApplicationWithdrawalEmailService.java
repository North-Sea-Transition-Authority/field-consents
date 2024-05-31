package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@Service
public class ApplicationWithdrawalEmailService {

  private static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for application withdrawal notification";
  static final String CASE_MANAGERS_RECIPIENT_DISPLAY_NAME = "Case Managers";

  private final EmailService emailService;
  private final TeamMemberViewService teamMemberViewService;
  private final EnergyPortalUserService energyPortalUserService;
  private final OrganisationUnitService organisationUnitService;

  public ApplicationWithdrawalEmailService(EmailService emailService,
                                           TeamMemberViewService teamMemberViewService,
                                           EnergyPortalUserService energyPortalUserService,
                                           OrganisationUnitService organisationUnitService) {
    this.emailService = emailService;
    this.teamMemberViewService = teamMemberViewService;
    this.energyPortalUserService = energyPortalUserService;
    this.organisationUnitService = organisationUnitService;
  }

  public void sendApplicationWithdrawalRequestEmail(ApplicationWithdrawal applicationWithdrawal) {
    var applicationVersion = applicationWithdrawal.getApplicationVersion();

    var primaryOperator = organisationUnitService.getOrganisationUnitByIdOrFallback(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE,
        applicationVersion.getCachedPrimaryOperatorName());

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

    // otherwise email all case managers
    var caseManagerEmailRecipients = teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER))
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .toList();

    var mergedTemplate = mergedTemplateBuilder
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME)
        .merge();

    caseManagerEmailRecipients.forEach(caseManagerEmailRecipient ->
        emailService.sendEmail(
            mergedTemplate,
            caseManagerEmailRecipient,
            applicationVersion
        ));
  }

  public void sendApplicationWithdrawalResponseEmail(ApplicationWithdrawal applicationWithdrawal) {
    var applicationVersion = applicationWithdrawal.getApplicationVersion();
    var withdrawalRequesterEmailRecipient = FieldConsentsEmailRecipient.from(
        energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationWithdrawal.getRequestedByWuaId()))
    );

    var mergedTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.APPLICATION_WITHDRAWAL_RESPONSE, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, withdrawalRequesterEmailRecipient.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        withdrawalRequesterEmailRecipient,
        applicationVersion
    );
  }
}
