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

  public static final String ORGANISATION_LOOKUP_PURPOSE = "Organisation lookup for consent issuing notification";

  private final EmailService emailService;
  private final IndustryTeamService industryTeamService;
  private final TeamMemberViewService teamMemberViewService;
  private final OrganisationUnitService organisationUnitService;
  private final EnergyPortalUserService energyPortalUserService;
  private final ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  public ConsentEmailService(EmailService emailService,
                             IndustryTeamService industryTeamService,
                             TeamMemberViewService teamMemberViewService,
                             OrganisationUnitService organisationUnitService,
                             EnergyPortalUserService energyPortalUserService,
                             ConsentFieldEquityPartnerService consentFieldEquityPartnerService) {
    this.emailService = emailService;
    this.industryTeamService = industryTeamService;
    this.teamMemberViewService = teamMemberViewService;
    this.organisationUnitService = organisationUnitService;
    this.energyPortalUserService = energyPortalUserService;
    this.consentFieldEquityPartnerService = consentFieldEquityPartnerService;
  }

  public void sendConsentIssuedEmailToOperator(ApplicationVersion applicationVersion) {
    var primaryOperator = organisationUnitService.getOrganisationUnitWithGroupsById(
        applicationVersion.getPrimaryOperatorOuId(),
        ORGANISATION_LOOKUP_PURPOSE);

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

    var distinctEmailRecipients = organisationUnitWithGroupsJson.organisationGroups().stream()
        .flatMap(organisationGroupDto ->
            industryTeamService.getTeamByOrganisationGroupId(organisationGroupDto.getOrganisationGroupId()).stream())
        .flatMap(team ->
            teamMemberViewService.getTeamMemberViewsWithRolesForTeam(team, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)).stream())
        .map(FieldConsentsEmailRecipient::from)
        .collect(Collectors.toSet());

    // iterate over the list of field equity partner consent recipients to notify about the consent being issued
    distinctEmailRecipients.forEach(recipient ->
        emailService.sendEmail(
            mergedTemplate,
            recipient,
            domainReference
        ));
  }
}
