package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil.ENERGY_PORTAL_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalEmailService.CASE_MANAGERS_RECIPIENT_DISPLAY_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawalTestUtil.getOpenApplicationWithdrawal;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.APPLICATION_VERSION_DOMAIN_REFERENCE;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_MANAGER_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_MANAGER_2;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER_EPU;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.TEAM_MEMBER_VIEW_CASE_MANAGER_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.TEAM_MEMBER_VIEW_CASE_MANAGER_2;

import java.util.Collections;
import java.util.List;
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
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class ApplicationWithdrawalEmailServiceTest {

  @Mock
  private EmailService emailService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private ApplicationWithdrawalEmailService applicationWithdrawalEmailService;

  private ApplicationVersion applicationVersion;

  private OrganisationUnitJson primaryOperator;

  private ApplicationWithdrawal applicationWithdrawal;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationWithdrawalEmailService = new ApplicationWithdrawalEmailService(
        emailService,
        teamMemberViewService,
        energyPortalUserService,
        organisationUnitService
    );
    primaryOperator = new OrganisationUnitJson(applicationVersion.getPrimaryOperatorOuId(), applicationVersion.getCachedPrimaryOperatorName());
    applicationWithdrawal = getOpenApplicationWithdrawal(applicationVersion);
  }

  @Test
  void sendApplicationWithdrawalRequestEmail_whenCaseOfficerIsCurrentOwner() {
    applicationVersion.setCaseOfficerWuaId(CASE_OFFICER.wuaId());

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);

    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_WITHDRAWAL_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(energyPortalUserService.getByWuaId(any())).thenReturn(CASE_OFFICER_EPU);

    applicationWithdrawalEmailService.sendApplicationWithdrawalRequestEmail(applicationWithdrawal);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_EPU.displayName()),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER_EPU).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendApplicationWithdrawalRequestEmail_whenCaseOfficerIsNotAssigned_withNoCaseManagersToNotify() {
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);

    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_WITHDRAWAL_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(Collections.emptyList());

    applicationWithdrawalEmailService.sendApplicationWithdrawalRequestEmail(applicationWithdrawal);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendApplicationWithdrawalRequestEmail_whenCaseOfficerIsNotAssigned_withOneCaseManagerToNotify() {
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);

    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_WITHDRAWAL_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CASE_MANAGER_1));

    applicationWithdrawalEmailService.sendApplicationWithdrawalRequestEmail(applicationWithdrawal);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_MANAGER_1).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendApplicationWithdrawalRequestEmail_whenCaseOfficerIsNotAssigned_withMultipleCaseManagersToNotify() {
    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()),
        anyString(),
        eq(applicationVersion.getCachedPrimaryOperatorName()))
    ).thenReturn(primaryOperator);

    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_WITHDRAWAL_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CASE_MANAGER_1, TEAM_MEMBER_VIEW_CASE_MANAGER_2));

    applicationWithdrawalEmailService.sendApplicationWithdrawalRequestEmail(applicationWithdrawal);

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
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME),
            tuple(PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD, primaryOperator.name())
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();
    assertThat(testEmailRecipients).hasSize(2);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_MANAGER_1).getEmailAddress());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_MANAGER_2).getEmailAddress());

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

  @Test
  void sendApplicationWithdrawalResponseEmail() {
    when(emailService.getTemplate(GovukNotifyTemplate.APPLICATION_WITHDRAWAL_RESPONSE, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(energyPortalUserService.getByWuaId(any())).thenReturn(ENERGY_PORTAL_USER_1);

    applicationWithdrawalEmailService.sendApplicationWithdrawalResponseEmail(applicationWithdrawal);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER_1.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER_1).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
}
