package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.SENDER_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.APPLICATION_VERSION_DOMAIN_REFERENCE;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CAM_USER;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_MANAGER_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_MANAGER_2;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER_EPU;
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
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentEmailServiceTest {

  @Mock
  private EmailService emailService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Captor
  private ArgumentCaptor<MergedTemplate> templateCaptor;

  @Captor
  private ArgumentCaptor<EmailRecipient> emailRecipientCaptor;

  @Captor
  private ArgumentCaptor<DomainReference>  domainReferenceCaptor;

  private CaseAssignmentEmailService caseAssignmentEmailService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    caseAssignmentEmailService = new CaseAssignmentEmailService(
        emailService,
        teamMemberViewService,
        energyPortalUserService
    );
  }

  @Test
  void sendCaseAssignmentEmail_whenAssignedToCaseOfficer() {
    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    caseAssignmentEmailService.sendCaseAssignmentEmail(
        applicationVersion,
        GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER,
        FieldConsentsEmailRecipient.from(CASE_OFFICER),
        CASE_MANAGER_1);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER.displayName()),
            tuple(SENDER_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGER_1.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendCaseAssignmentEmail_whenAssignedToCamUser() {
    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CASE_ASSIGNED_TO_CAM_USER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    caseAssignmentEmailService.sendCaseAssignmentEmail(
        applicationVersion,
        GovukNotifyTemplate.CASE_ASSIGNED_TO_CAM_USER,
        FieldConsentsEmailRecipient.from(CAM_USER),
        CASE_OFFICER);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CAM_USER.displayName()),
            tuple(SENDER_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CAM_USER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendCaseOwnershipReleasedEmail_withNoCaseManagersToNotify() {
    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(Collections.emptyList());

    caseAssignmentEmailService.sendCaseOwnershipReleasedEmail(applicationVersion, CASE_OFFICER);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendCaseOwnershipReleasedEmail_withOneCaseManagerToNotify() {
    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CASE_MANAGER_1));

    caseAssignmentEmailService.sendCaseOwnershipReleasedEmail(applicationVersion, CASE_OFFICER);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(SENDER_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER.displayName()),
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGER_1.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_MANAGER_1).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendCaseOwnershipReleasedEmail_withMultipleCaseManagersToNotify() {
    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CASE_MANAGER_1, TEAM_MEMBER_VIEW_CASE_MANAGER_2));

    caseAssignmentEmailService.sendCaseOwnershipReleasedEmail(applicationVersion, CASE_OFFICER);

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
            tuple(SENDER_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER.displayName()),
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGER_1.displayName())
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(SENDER_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER.displayName()),
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGER_2.displayName())
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
  void sendCaseReturnedToCaseOfficerByCamEmail() {
    when(emailService.getTemplateForApplication(GovukNotifyTemplate.CASE_RETURNED_TO_CASE_OFFICER_BY_CAM_USER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    applicationVersion.setCaseOfficerWuaId(CASE_OFFICER.wuaId());
    when(energyPortalUserService.getByWuaId(any())).thenReturn(CASE_OFFICER_EPU);

    caseAssignmentEmailService.sendCaseReturnedToCaseOfficerByCamEmail(applicationVersion, CAM_USER);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_EPU.displayName()),
            tuple(SENDER_IDENTIFIER_MERGE_FIELD_NAME, CAM_USER.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER_EPU).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
}
