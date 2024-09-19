package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.APPLICATION_VERSION_DOMAIN_REFERENCE;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER_EPU;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.ENERGY_PORTAL_USER_DTO;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit4;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.CONSENT_RECIPIENT;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.CREATOR;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.EDITOR;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole.SUBMITTER;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.Template;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartner;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipientService;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamService;

@ExtendWith(MockitoExtension.class)
class ConsentEmailServiceTest {

  static final Team INDUSTRY_TEAM_1 = new TeamTestUtil.TeamBuilder()
      .withId(1)
      .withTeamType(TeamType.INDUSTRY)
      .build();

  private static final TeamMemberView TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1 = new TeamMemberView(
      WebUserAccountId.valueOf("10"),
      new TeamView(INDUSTRY_TEAM_1.toTeamId(), TeamType.INDUSTRY, "Industry team"),
      "Mr",
      "Consent1",
      "Recipient1",
      "industry.recipient1@email.co.uk",
      "012345",
      Set.of(IndustryTeamRole.CONSENT_RECIPIENT)
  );

  private static final TeamMemberView TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2 = new TeamMemberView(
      WebUserAccountId.valueOf("20"),
      new TeamView(INDUSTRY_TEAM_1.toTeamId(), TeamType.INDUSTRY, "Consent team"),
      "Mr",
      "Consent2",
      "Recipient2",
      "industry.recipient2@email.co.uk",
      "06789",
      Set.of(IndustryTeamRole.CONSENT_RECIPIENT)
  );

  private static final TeamMemberView TEAM_MEMBER_VIEW_CREATOR = new TeamMemberView(
      WebUserAccountId.valueOf("30"),
      new TeamView(INDUSTRY_TEAM_1.toTeamId(), TeamType.INDUSTRY, "Consent team"),
      "Mr",
      "Creator",
      "Creator Surname",
      "industry.creator@email.co.uk",
      "23412",
      Set.of(CREATOR)
  );

  private static final TeamMemberView TEAM_MEMBER_VIEW_SUBMITTER = new TeamMemberView(
      WebUserAccountId.valueOf("40"),
      new TeamView(INDUSTRY_TEAM_1.toTeamId(), TeamType.INDUSTRY, "Consent team"),
      "Mr",
      "Submitter",
      "Submitter Surname",
      "industry.submitter@email.co.uk",
      "08923",
      Set.of(SUBMITTER)
  );

  private static final TeamMemberView TEAM_MEMBER_VIEW_EDITOR = new TeamMemberView(
      WebUserAccountId.valueOf("50"),
      new TeamView(INDUSTRY_TEAM_1.toTeamId(), TeamType.INDUSTRY, "Consent team"),
      "Mr",
      "Editor",
      "Editor Surname",
      "industry.editor@email.co.uk",
      "98723",
      Set.of(EDITOR)
  );

  @Mock
  private EmailService emailService;

  @Mock
  private IndustryTeamService industryTeamService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  @Mock
  private FieldConsentsEmailRecipientService fieldConsentsEmailRecipientService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private ConsentEmailService consentEmailService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    consentEmailService = new ConsentEmailService(
        emailService,
        industryTeamService,
        teamMemberViewService,
        organisationUnitService,
        energyPortalUserService,
        consentFieldEquityPartnerService,
        fieldConsentsEmailRecipientService
    );
  }

  @Test
  void sendConsentIssuedEmailToOperator_withNoTeamsFoundWithinTheOrganisationGroup_thenOnlyApplicationSubmitterIsNotified() {
    var serviceDetailSubmitter = FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER_DTO);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString())
    ).thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit1));

    when(energyPortalUserService.getByWuaId(any()))
        .thenReturn(ENERGY_PORTAL_USER_DTO);

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.empty());

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    consentEmailService.sendConsentIssuedEmailToOperator(applicationVersion);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CACHED_PRIMARY_OPERATOR_NAME_1)
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(serviceDetailSubmitter.getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsentIssuedEmailToOperator_withTeamFoundButNoMemberRolesOtherThanSubmitter_thenOnlyApplicationSubmitterIsNotified() {
    var serviceDetailSubmitter = FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER_DTO);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString())
    ).thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit1));

    when(energyPortalUserService.getByWuaId(any()))
        .thenReturn(ENERGY_PORTAL_USER_DTO);

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(CONSENT_RECIPIENT, CREATOR, SUBMITTER, EDITOR)))
        .thenReturn(Collections.emptyList());
    
    consentEmailService.sendConsentIssuedEmailToOperator(applicationVersion);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CACHED_PRIMARY_OPERATOR_NAME_1)
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(serviceDetailSubmitter.getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsentIssuedEmailToOperator_withTeamFoundAndMultipleMembersInDifferentRoles_thenNotifyAll() {
    var serviceDetailSubmitter = FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER_DTO);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString())
    ).thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit1));

    when(energyPortalUserService.getByWuaId(any()))
        .thenReturn(ENERGY_PORTAL_USER_DTO);

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(CONSENT_RECIPIENT, CREATOR, SUBMITTER, EDITOR)))
        .thenReturn(List.of(
            TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1,
            TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2,
            TEAM_MEMBER_VIEW_CREATOR,
            TEAM_MEMBER_VIEW_SUBMITTER,
            TEAM_MEMBER_VIEW_EDITOR));

    consentEmailService.sendConsentIssuedEmailToOperator(applicationVersion);

    verify(emailService, Mockito.times(6)).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    // verify emails merge fields
    var emailTemplates = templateCaptor.getAllValues();
    var domainReferences = domainReferenceCaptor.getAllValues();

    emailTemplates.forEach(emailTemplate ->
        assertThat(emailTemplate.getMailMergeFields())
            .extracting(MailMergeField::name, MailMergeField::value)
            .containsOnly(
                tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CACHED_PRIMARY_OPERATOR_NAME_1)));

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();

    assertThat(testEmailRecipients).contains(
        serviceDetailSubmitter,
        FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1),
        FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2),
        FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CREATOR),
        FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_SUBMITTER),
        FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_EDITOR));

    // verify domain references
    domainReferences.forEach(domainReference -> {
      assertThat(domainReference.getDomainId())
          .isEqualTo(applicationVersion.getId().toString());

      assertThat(domainReference.getDomainType())
          .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
    });
  }

  @Test
  void sendConsentIssuedEmailToCaseOfficer() {
    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    applicationVersion.setCaseOfficerWuaId(CASE_OFFICER.wuaId());
    when(energyPortalUserService.getByWuaId(any())).thenReturn(CASE_OFFICER_EPU);

    consentEmailService.sendConsentIssuedEmailToCaseOfficer(applicationVersion);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_EPU.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER_EPU).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsentIssuedEmailToFieldEquityPartners_whenNoFieldEquityPartners_thenNoEmailIsSent() {
    var consent = new Consent(1);

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(Collections.emptyList());

    consentEmailService.sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendConsentIssuedEmailToFieldEquityPartners_whenFieldEquityPartnerButNoMemberInConsentRecipientRole_thenNoEmailIsSent() {
    var consent = new Consent(1);
    var consentFieldEquityPartners = List.of(
        new ConsentFieldEquityPartner(consent, 1, "org A", "reg A"));
    var organisationUnitJson = OrganisationUnitWithGroupsJson.from(orgUnit1);

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(consentFieldEquityPartners);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(organisationUnitJson);

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(fieldConsentsEmailRecipientService
        .getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(Collections.emptySet());

    consentEmailService.sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendConsentIssuedEmailToFieldEquityPartners_whenFieldEquityPartnerAndOneMemberInConsentRecipientRole_thenSendEmail() {
    var consent = new Consent(1);
    var consentFieldEquityPartners = List.of(
        new ConsentFieldEquityPartner(consent, 1, "org A", "reg A"));
    var organisationUnitJson = OrganisationUnitWithGroupsJson.from(orgUnit1);

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(consentFieldEquityPartners);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(organisationUnitJson);

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(fieldConsentsEmailRecipientService
        .getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(Set.of(FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1)));

    consentEmailService.sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, "org A")
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1.contactEmail());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsentIssuedEmailToFieldEquityPartners_whenFieldEquityPartnerAndMultipleOrgGroupsWithTheSameMemberInConsentRecipientRole_thenSendOneEmailOnly() {
    var consent = new Consent(1);
    var consentFieldEquityPartners = List.of(
        new ConsentFieldEquityPartner(consent, 1, "org A", "reg A"));
    var organisationUnitJson = OrganisationUnitWithGroupsJson.from(orgUnit4);

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(consentFieldEquityPartners);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(organisationUnitJson);

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(fieldConsentsEmailRecipientService
        .getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(Set.of(FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1)));

    consentEmailService.sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, "org A")
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1.contactEmail());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsentIssuedEmailToFieldEquityPartners_whenFieldEquityPartnerAndMultipleMembersInConsentRecipientRole_thenSendEmail() {
    var consent = new Consent(1);
    var consentFieldEquityPartners = List.of(
        new ConsentFieldEquityPartner(consent, 1, "org A", "reg A"));
    var organisationUnitJson = OrganisationUnitWithGroupsJson.from(orgUnit1);

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(consentFieldEquityPartners);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(organisationUnitJson);

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(fieldConsentsEmailRecipientService
        .getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(Set.of(
            FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1),
            FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2)));

    consentEmailService.sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);

    verify(emailService, Mockito.times(2)).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    // verify emails merge fields
    var emailTemplates = templateCaptor.getAllValues();
    var domainReferences = domainReferenceCaptor.getAllValues();

    var firstEmailMergeFields = emailTemplates.get(0).getMailMergeFields();
    assertThat(firstEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, "org A")
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, "org A")
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();

    assertThat(testEmailRecipients).containsExactlyInAnyOrder(
        FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1),
        FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2)
    );

    // verify domain references
    assertThat(domainReferences)
        .extracting(DomainReference::getDomainId, DomainReference::getDomainType)
        .containsOnly(
            tuple(applicationVersion.getId().toString(), APPLICATION_VERSION_DOMAIN_REFERENCE));
  }
}
