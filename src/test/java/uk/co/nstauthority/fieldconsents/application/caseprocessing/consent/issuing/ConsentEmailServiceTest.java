package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.APPLICATION_VERSION_DOMAIN_REFERENCE;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER_EPU;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit4;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitWithGroupsJson;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@ExtendWith(MockitoExtension.class)
class ConsentEmailServiceTest {

  private static final Team INDUSTRY_TEAM_1 = TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).build();

  private static final TeamMemberView TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1 = TeamMemberViewTestUtil.newBuilder()
      .withTeam(INDUSTRY_TEAM_1)
      .withRoles(Role.CONSENT_RECIPIENT)
      .build();

  private static final TeamMemberView TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2 = TeamMemberViewTestUtil.newBuilder()
      .withTeam(INDUSTRY_TEAM_1)
      .withRoles(Role.CONSENT_RECIPIENT)
      .build();

  @Mock
  private EmailService emailService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  @Mock
  private FieldConsentsEmailRecipientService fieldConsentsEmailRecipientService;

  @Mock
  private TeamQueryService teamQueryService;

  @Spy
  @InjectMocks
  private ConsentEmailService consentEmailService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void sendConsentIssuedEmailToOperator_multipleRecipients() {
    var emailRecipients = Set.of(
        FieldConsentsEmailRecipient.from(TeamMemberViewTestUtil.newBuilder().build()),
        FieldConsentsEmailRecipient.from(TeamMemberViewTestUtil.newBuilder().build()),
        FieldConsentsEmailRecipient.from(TeamMemberViewTestUtil.newBuilder().build())
    );

    var primaryOperator = OrganisationUnitWithGroupsJson.from(orgUnit1);

    when(organisationUnitService.getOrganisationUnitWithGroupsById(eq(applicationVersion.getPrimaryOperatorOuId()), anyString()))
        .thenReturn(primaryOperator);

    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CONSENT_ISSUED_TO_OPERATOR, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    doReturn(emailRecipients)
        .when(consentEmailService).getConsentIssuedEmailRecipientsForOperator(applicationVersion, primaryOperator);

    consentEmailService.sendConsentIssuedEmailToOperator(applicationVersion);

    verify(emailService, times(emailRecipients.size())).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CACHED_PRIMARY_OPERATOR_NAME_1)
        );

    assertThat(emailRecipientCaptor.getAllValues())
        .containsExactlyInAnyOrderElementsOf(emailRecipients);

    assertThat(domainReferenceCaptor.getAllValues())
        .map(DomainReference::getDomainId)
        .allMatch(applicationVersion.getId().toString()::equals);

    assertThat(domainReferenceCaptor.getAllValues())
        .map(DomainReference::getDomainType)
        .allMatch(APPLICATION_VERSION_DOMAIN_REFERENCE::equals);
  }

  @Test
  void getConsentIssuedEmailRecipientsForOperator() {
    var primaryOperator = OrganisationUnitWithGroupsJson.from(orgUnit1);

    var teamScopeIds = primaryOperator.organisationGroups()
        .stream()
        .map(OrganisationGroupDto::getOrganisationGroupId)
        .map(String::valueOf)
        .collect(Collectors.toSet());

    var nonSubmitterTeamRoles = List.of(
        TeamRoleTestUtil.newBuilder().withRole(Role.CONSENT_RECIPIENT).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.CREATOR).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.SUBMITTER).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.EDITOR).build(),
        TeamRoleTestUtil.newBuilder().withRole(Role.ACCESS_MANAGER).build() // should be ignored
    );
    var submitterTeamRole = TeamRoleTestUtil.newBuilder()
        .withRole(Role.FINANCE_ADMINISTRATOR) // maybe they got a promotion or something. They were a submitter at one point
        .build();

    var emailRecipientTeamRoles = new HashSet<>(nonSubmitterTeamRoles.subList(0, 4));
    emailRecipientTeamRoles.add(submitterTeamRole);

    var teamMemberViews = List.of(
        TeamMemberViewTestUtil.newBuilder().build()
    );

    var expectedRecipients = teamMemberViews
        .stream()
        .map(FieldConsentsEmailRecipient::from)
        .collect(Collectors.toSet());

    when(teamQueryService.getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds))
        .thenReturn(nonSubmitterTeamRoles);

    when(teamQueryService.getTeamRoles(WebUserAccountId.from(applicationVersion.getSubmittedByWuaId())))
        .thenReturn(List.of(submitterTeamRole));

    when(teamQueryService.getTeamMemberViews(emailRecipientTeamRoles))
        .thenReturn(teamMemberViews);

    assertThat(consentEmailService.getConsentIssuedEmailRecipientsForOperator(applicationVersion, primaryOperator))
        .isEqualTo(expectedRecipients);
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
        .getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(Role.CONSENT_RECIPIENT)))
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
        .getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(Role.CONSENT_RECIPIENT)))
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
        .isEqualTo(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1.email());

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
        .getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(Role.CONSENT_RECIPIENT)))
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
        .isEqualTo(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1.email());

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
        .getDistinctEmailRecipientsWithRoles(organisationUnitJson, Set.of(Role.CONSENT_RECIPIENT)))
        .thenReturn(Set.of(
            FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_1),
            FieldConsentsEmailRecipient.from(TEAM_MEMBER_VIEW_CONSENT_RECIPIENT_2)));

    consentEmailService.sendConsentIssuedEmailToFieldEquityPartners(applicationVersion, consent);

    verify(emailService, times(2)).sendEmail(
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
