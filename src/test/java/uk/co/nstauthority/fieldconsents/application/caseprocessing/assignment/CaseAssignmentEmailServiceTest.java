package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentEmailServiceTest {

  private static final ServiceUserDetail CASE_OFFICER = ServiceUserDetailTestUtil.Builder()
      .withForename("Case")
      .withSurname("Officer")
      .withEmailAddress("case.officer@email.co.uk")
      .build();

  private static final ServiceUserDetail CASE_MANAGER_1 = ServiceUserDetailTestUtil.Builder()
      .withForename("Case1")
      .withSurname("Manager1")
      .withEmailAddress("case.manager1@email.co.uk")
      .withWuaId(2L)
      .build();

  private static final ServiceUserDetail CASE_MANAGER_2 = ServiceUserDetailTestUtil.Builder()
      .withForename("Case2")
      .withSurname("Manager2")
      .withEmailAddress("case.manager2@email.co.uk")
      .withWuaId(3L)
      .build();

  private static final Team REGULATOR_TEAM = new TeamTestUtil.TeamBuilder()
      .withId(1)
      .withTeamType(TeamType.REGULATOR)
      .build();

  private static final String APPLICATION_VERSION_DOMAIN_REFERENCE = "APPLICATION_VERSION";

  private static final TeamMemberView TEAM_MEMBER_VIEW_CASE_MANAGER_1 = new TeamMemberView(
      WebUserAccountId.from(CASE_MANAGER_1),
      new TeamView(REGULATOR_TEAM.toTeamId(), TeamType.REGULATOR, "Regulator team"),
      "Mr",
      "Case1",
      "Manager1",
      "case.manager1@email.co.uk",
      "012345",
      Set.of(RegulatorTeamRole.CASE_MANAGER)
  );

  private static final TeamMemberView TEAM_MEMBER_VIEW_CASE_MANAGER_2 = new TeamMemberView(
      WebUserAccountId.from(CASE_MANAGER_1),
      new TeamView(REGULATOR_TEAM.toTeamId(), TeamType.REGULATOR, "Regulator team"),
      "Mr",
      "Case2",
      "Manager2",
      "case.manager2@email.co.uk",
      "06789",
      Set.of(RegulatorTeamRole.CASE_MANAGER)
  );

  @Mock
  private EmailService emailService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  private CaseAssignmentEmailService caseAssignmentEmailService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    caseAssignmentEmailService = new CaseAssignmentEmailService(emailService, teamMemberViewService);
  }

  @Test
  void sendCaseAssignmentEmail() {
    when(emailService.getTemplate(GovukNotifyTemplate.CASE_ASSIGNED_TO_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    caseAssignmentEmailService.sendCaseAssignmentEmail(applicationVersion, FieldConsentsEmailRecipient.from(CASE_OFFICER), CASE_MANAGER_1);

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple("CASE_OFFICER", CASE_OFFICER.displayName()),
            tuple("CASE_ASSIGNEE", CASE_MANAGER_1.displayName())
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendCaseOwnershipReleasedEmail_withNoCaseManagersToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(Collections.emptyList());

    caseAssignmentEmailService.sendCaseOwnershipReleasedEmail(applicationVersion, CASE_OFFICER);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendCaseOwnershipReleasedEmail_withOneCaseManagerToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CASE_MANAGER_1));

    caseAssignmentEmailService.sendCaseOwnershipReleasedEmail(applicationVersion, CASE_OFFICER);

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple("CASE_OFFICER", CASE_OFFICER.displayName()),
            tuple("CASE_MANAGER", CASE_MANAGER_1.displayName())
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
    when(emailService.getTemplate(GovukNotifyTemplate.CASE_RELEASED_BY_CASE_OFFICER, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CASE_MANAGER_1, TEAM_MEMBER_VIEW_CASE_MANAGER_2));

    caseAssignmentEmailService.sendCaseOwnershipReleasedEmail(applicationVersion, CASE_OFFICER);

    var templateCaptor = ArgumentCaptor.forClass(MergedTemplate.class);
    var emailRecipientCaptor = ArgumentCaptor.forClass(EmailRecipient.class);
    var domainReferenceCaptor = ArgumentCaptor.forClass(DomainReference.class);

    verify(emailService, Mockito.times(2)).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    // verify emails merge fields
    var emailTemplates = templateCaptor.getAllValues();

    var firstEmailMergeFields = emailTemplates.get(0).getMailMergeFields();
    assertThat(firstEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple("CASE_OFFICER", CASE_OFFICER.displayName()),
            tuple("CASE_MANAGER", CASE_MANAGER_1.displayName())
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple("CASE_OFFICER", CASE_OFFICER.displayName()),
            tuple("CASE_MANAGER", CASE_MANAGER_2.displayName())
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();
    assertThat(testEmailRecipients).hasSize(2);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_MANAGER_1).getEmailAddress());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_MANAGER_2).getEmailAddress());

    // verify domain reference
    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
}
