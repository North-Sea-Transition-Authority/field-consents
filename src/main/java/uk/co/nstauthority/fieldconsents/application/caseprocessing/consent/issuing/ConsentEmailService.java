package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipientService;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class ConsentEmailService {

  public static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for consent issuing notification";

  private final EmailService emailService;
  private final OrganisationUnitService organisationUnitService;
  private final EnergyPortalUserService energyPortalUserService;
  private final ConsentFieldEquityPartnerService consentFieldEquityPartnerService;
  private final FieldConsentsEmailRecipientService fieldConsentsEmailRecipientService;
  private final TeamQueryService teamQueryService;

  ConsentEmailService(
      EmailService emailService,
      OrganisationUnitService organisationUnitService,
      EnergyPortalUserService energyPortalUserService,
      ConsentFieldEquityPartnerService consentFieldEquityPartnerService,
      FieldConsentsEmailRecipientService fieldConsentsEmailRecipientService,
      TeamQueryService teamQueryService
  ) {
    this.emailService = emailService;
    this.organisationUnitService = organisationUnitService;
    this.energyPortalUserService = energyPortalUserService;
    this.consentFieldEquityPartnerService = consentFieldEquityPartnerService;
    this.fieldConsentsEmailRecipientService = fieldConsentsEmailRecipientService;
    this.teamQueryService = teamQueryService;
  }

  public void sendConsentIssuedEmailToOperator(ApplicationVersion applicationVersion) {
    var primaryOperator = organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE
    );

    var emailRecipients = getConsentIssuedEmailRecipientsForOperator(applicationVersion, primaryOperator);

    var emailMergedTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion)
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

  Set<FieldConsentsEmailRecipient> getConsentIssuedEmailRecipientsForOperator(
      ApplicationVersion applicationVersion,
      OrganisationUnitWithGroupsJson primaryOperator
  ) {
    var teamScopeIds = primaryOperator.organisationGroups().stream()
        .map(OrganisationGroupDto::getOrganisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    var emailRecipientTeamRoles = new HashSet<TeamRole>();

    teamQueryService
        .getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds)
        .stream()
        .filter(teamrole ->
            teamrole.getRole() == Role.CONSENT_RECIPIENT
            || teamrole.getRole() == Role.CREATOR
            || teamrole.getRole() == Role.SUBMITTER
            || teamrole.getRole() == Role.EDITOR
        )
        .forEach(emailRecipientTeamRoles::add);

    // also include the user who submitted the application, in case their role changed and is no longer part of the above
    var applicationSubmitter = applicationVersion.getSubmittedByWuaId();
    emailRecipientTeamRoles.addAll(teamQueryService.getTeamRoles(WebUserAccountId.from(applicationSubmitter)));

    return teamQueryService.getTeamMemberViews(emailRecipientTeamRoles)
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .collect(Collectors.toSet());
  }

  public void sendConsentIssuedEmailToCaseOfficer(ApplicationVersion applicationVersion) {
    var caseOfficer = FieldConsentsEmailRecipient.from(
        energyPortalUserService.getByWuaId(WebUserAccountId.from(applicationVersion.getCaseOfficerWuaId()))
    );

    MergedTemplate mergedTemplate = emailService
        .getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_CASE_OFFICER, applicationVersion)
        .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, caseOfficer.displayName())
        .merge();

    emailService.sendEmail(
        mergedTemplate,
        caseOfficer,
        applicationVersion
    );
  }

  public void sendConsentIssuedEmailToFieldEquityPartners(ApplicationVersion applicationVersion, Consent consent) {
    var consentFieldEquityPartners = consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent);

    consentFieldEquityPartners.forEach(consentFieldEquityPartner -> {
      var emailMergedTemplate = emailService
          .getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion)
          .withMailMergeField(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, consentFieldEquityPartner.getOrganisationName())
          .merge();

      var organisationUnitWithGroupsJson = organisationUnitService
          .getOrganisationUnitWithGroupsById(consentFieldEquityPartner.getOrganisationUnitId(), ORGANISATION_LOOKUP_PURPOSE);

      sendConsentIssuedEmailToFieldEquityPartner(organisationUnitWithGroupsJson, applicationVersion, emailMergedTemplate);
    });
  }

  // Sends an email to all the consent recipients of the organisation groups this consentFieldEquityPartner
  // is part of. If user is consent recipient in more than one organisation group, we only send an email
  // per consentFieldEquityPartner.
  public void sendConsentIssuedEmailToFieldEquityPartner(
      OrganisationUnitWithGroupsJson organisationUnitWithGroupsJson,
      DomainReference domainReference,
      MergedTemplate mergedTemplate
  ) {
    var distinctEmailRecipients = fieldConsentsEmailRecipientService.getDistinctEmailRecipientsWithRoles(
        organisationUnitWithGroupsJson,
        Set.of(Role.CONSENT_RECIPIENT)
    );

    // iterate over the list of field equity partner consent recipients to notify about the consent being issued
    distinctEmailRecipients.forEach(recipient ->
        emailService.sendEmail(
            mergedTemplate,
            recipient,
            domainReference
        ));
  }
}
