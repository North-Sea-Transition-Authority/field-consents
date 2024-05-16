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
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.ORG_GROUP_2;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit4;

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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
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

  static final Team INDUSTRY_TEAM_2 = new TeamTestUtil.TeamBuilder()
      .withId(2)
      .withTeamType(TeamType.INDUSTRY)
      .build();

  private static final ServiceUserDetail CONSENT_RECIPIENT_1 = ServiceUserDetailTestUtil.Builder()
      .withForename("Consent1")
      .withSurname("Recipient1")
      .withEmailAddress("industry.recipient1@email.co.uk")
      .withWuaId(1L)
      .build();

  private static final ServiceUserDetail CONSENT_RECIPIENT_2 = ServiceUserDetailTestUtil.Builder()
      .withForename("Consent2")
      .withSurname("Recipient2")
      .withEmailAddress("industry.recipient2@email.co.uk")
      .withWuaId(2L)
      .build();
  
  private static final TeamMemberView TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1 = new TeamMemberView(
      WebUserAccountId.from(CONSENT_RECIPIENT_1),
      new TeamView(INDUSTRY_TEAM_1.toTeamId(), TeamType.INDUSTRY, "Industry team"),
      "Mr",
      "Consent1",
      "Recipient1",
      "industry.recipient1@email.co.uk",
      "012345",
      Set.of(IndustryTeamRole.CONSENT_RECIPIENT)
  );

  private static final TeamMemberView TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2 = new TeamMemberView(
      WebUserAccountId.from(CONSENT_RECIPIENT_2),
      new TeamView(INDUSTRY_TEAM_1.toTeamId(), TeamType.INDUSTRY, "Consent team"),
      "Mr",
      "Consent2",
      "Recipient2",
      "industry.recipient2@email.co.uk",
      "06789",
      Set.of(IndustryTeamRole.CONSENT_RECIPIENT)
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
        consentFieldEquityPartnerService
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

    when(emailService.getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion))
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
  void sendConsentIssuedEmailToOperator_withTeamFoundButNoMembersInTheConsentRecipientRole_thenOnlyApplicationSubmitterIsNotified() {
    var serviceDetailSubmitter = FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER_DTO);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString())
    ).thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit1));

    when(energyPortalUserService.getByWuaId(any()))
        .thenReturn(ENERGY_PORTAL_USER_DTO);

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(emailService.getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
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
  void sendConsentIssuedEmailToOperator_withTeamFoundAndMultipleMembersInTheConsentRecipientRole_thenNotifyAll() {
    var serviceDetailSubmitter = FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER_DTO);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString())
    ).thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit1));

    when(energyPortalUserService.getByWuaId(any()))
        .thenReturn(ENERGY_PORTAL_USER_DTO);

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(emailService.getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1, TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2));

    consentEmailService.sendConsentIssuedEmailToOperator(applicationVersion);

    verify(emailService, Mockito.times(3)).sendEmail(
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
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CACHED_PRIMARY_OPERATOR_NAME_1)
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CACHED_PRIMARY_OPERATOR_NAME_1)
        );

    var thirdEmailMergeFields = emailTemplates.get(2).getMailMergeFields();
    assertThat(thirdEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CACHED_PRIMARY_OPERATOR_NAME_1)
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();
    assertThat(testEmailRecipients).hasSize(3);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(serviceDetailSubmitter.getEmailAddress());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CONSENT_RECIPIENT_2).getEmailAddress());
    assertThat(testEmailRecipients.get(2).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CONSENT_RECIPIENT_1).getEmailAddress());

    // verify domain references
    assertThat(domainReferences.get(0).getDomainId())
        .isEqualTo(applicationVersion.getId().toString());
    assertThat(domainReferences.get(1).getDomainId())
        .isEqualTo(applicationVersion.getId().toString());
    assertThat(domainReferences.get(2).getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferences.get(0).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
    assertThat(domainReferences.get(1).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
    assertThat(domainReferences.get(2).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsentIssuedEmailToCaseOfficer() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_CASE_OFFICER, applicationVersion))
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

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(consentFieldEquityPartners);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit1));

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(emailService.getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(Collections.emptyList());

    consentEmailService.sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendConsentIssuedEmailToFieldEquityPartners_whenFieldEquityPartnerAndOneMemberInConsentRecipientRole_thenSendEmail() {
    var consent = new Consent(1);
    var consentFieldEquityPartners = List.of(
        new ConsentFieldEquityPartner(consent, 1, "org A", "reg A"));

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(consentFieldEquityPartners);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit1));

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(emailService.getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1));

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

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(consentFieldEquityPartners);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit4));

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_2.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_2));

    when(emailService.getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_2, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1));

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

    when(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).thenReturn(consentFieldEquityPartners);
    when(organisationUnitService.getOrganisationUnitWithGroupsById(any(), any()))
        .thenReturn(OrganisationUnitWithGroupsJson.from(orgUnit1));

    when(industryTeamService.getTeamByOrganisationGroupId(ORG_GROUP_1.getOrganisationGroupId()))
        .thenReturn(Optional.of(INDUSTRY_TEAM_1));

    when(emailService.getTemplate(GovukNotifyTemplate.CONSENT_ISSUED_TO_FIELD_EQUITY_PARTNER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(INDUSTRY_TEAM_1, Set.of(IndustryTeamRole.CONSENT_RECIPIENT)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1, TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2));

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
    assertThat(testEmailRecipients).hasSize(2);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2.contactEmail());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1.contactEmail());

    // verify domain references
    assertThat(domainReferences.get(0).getDomainId())
        .isEqualTo(applicationVersion.getId().toString());
    assertThat(domainReferences.get(1).getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferences.get(0).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
    assertThat(domainReferences.get(1).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
}
