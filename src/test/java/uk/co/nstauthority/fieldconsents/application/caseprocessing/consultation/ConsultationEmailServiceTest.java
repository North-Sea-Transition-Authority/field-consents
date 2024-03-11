package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationServiceTest.CONSULTATION_TEAM;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUESTER_USER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemIntegrationTestUtil.ENERGY_PORTAL_USER_DTO;

import java.time.Instant;
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
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.email.EmailService;
import uk.co.nstauthority.fieldconsents.email.FieldConsentsEmailRecipient;
import uk.co.nstauthority.fieldconsents.email.GovukNotifyTemplate;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole;

@ExtendWith(MockitoExtension.class)
class ConsultationEmailServiceTest {

  private static final ServiceUserDetail CONSULTEE_ALLOCATOR_1 = ServiceUserDetailTestUtil.Builder()
      .withForename("Consultee1")
      .withSurname("Allocator1")
      .withEmailAddress("opred.allocator1@email.co.uk")
      .withWuaId(1L)
      .build();

  private static final ServiceUserDetail CONSULTEE_ALLOCATOR_2 = ServiceUserDetailTestUtil.Builder()
      .withForename("Consultee2")
      .withSurname("Allocator2")
      .withEmailAddress("opred.allocator2@email.co.uk")
      .withWuaId(2L)
      .build();

  private static final TeamMemberView TEAM_MEMBER_VIEW_CONSULTEE_ALLOCATOR_1 = new TeamMemberView(
      WebUserAccountId.from(CONSULTEE_ALLOCATOR_1),
      new TeamView(CONSULTATION_TEAM.toTeamId(), TeamType.OPRED, "Consultee team"),
      "Mr",
      "Consultee1",
      "Allocator1",
      "opred.allocator1@email.co.uk",
      "012345",
      Set.of(OpredTeamRole.ALLOCATOR)
  );

  private static final TeamMemberView TEAM_MEMBER_VIEW_CONSULTEE_ALLOCATOR_2 = new TeamMemberView(
      WebUserAccountId.from(CONSULTEE_ALLOCATOR_2),
      new TeamView(CONSULTATION_TEAM.toTeamId(), TeamType.OPRED, "Consultee team"),
      "Mr",
      "Consultee2",
      "Allocator2",
      "opred.allocator2@email.co.uk",
      "06789",
      Set.of(OpredTeamRole.ALLOCATOR)
  );

  private static final String APPLICATION_VERSION_DOMAIN_REFERENCE = "APPLICATION_VERSION";

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

  private ConsultationEmailService consultationEmailService;

  private ApplicationVersion applicationVersion;

  private Consultation consultation;

  private String consultationDeadline;
  
  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    var requestDeadlineInstant = Instant.now();
    consultationDeadline = DateUtils.format(requestDeadlineInstant, DATE_TIME);
    consultation = new Consultation();
    consultation.setConsultationTeam(CONSULTATION_TEAM);
    consultation.setRequestDeadline(requestDeadlineInstant);
    consultation.setRequestApplicationVersion(applicationVersion);
    consultation.setResponderWuaId(ENERGY_PORTAL_USER_DTO.webUserAccountId());

    consultationEmailService = new ConsultationEmailService(emailService, teamMemberViewService, energyPortalUserService);
  }

  @Test
  void sendConsultationRequestEmail_withNoConsulteeAllocatorsToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.OPRED, Set.of(OpredTeamRole.ALLOCATOR)))
        .thenReturn(Collections.emptyList());

    consultationEmailService.sendConsultationRequestEmail(consultation);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendConsultationRequestEmail_withOneConsulteeAllocatorToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.OPRED, Set.of(OpredTeamRole.ALLOCATOR)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSULTEE_ALLOCATOR_1));

    consultationEmailService.sendConsultationRequestEmail(consultation);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CONSULTATION_TEAM.getTeamType().getDisplayText()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, consultationDeadline)
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CONSULTEE_ALLOCATOR_1).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsultationRequestEmail_withMultipleConsulteeAllocatorsToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUEST, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.OPRED, Set.of(OpredTeamRole.ALLOCATOR)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSULTEE_ALLOCATOR_1, TEAM_MEMBER_VIEW_CONSULTEE_ALLOCATOR_2));

    consultationEmailService.sendConsultationRequestEmail(consultation);

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
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CONSULTATION_TEAM.getTeamType().getDisplayText()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, consultationDeadline)
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CONSULTATION_TEAM.getTeamType().getDisplayText()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, consultationDeadline)
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();
    assertThat(testEmailRecipients).hasSize(2);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CONSULTEE_ALLOCATOR_1).getEmailAddress());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CONSULTEE_ALLOCATOR_2).getEmailAddress());

    // verify domain reference
    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
  
  @Test
  void sendConsultationAssignmentEmail() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_ASSIGNMENT, applicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(energyPortalUserService.getByWuaId(any())).thenReturn(ENERGY_PORTAL_USER_DTO);

    consultationEmailService.sendConsultationAssignmentEmail(consultation, CONSULTEE_ALLOCATOR_1);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, ENERGY_PORTAL_USER_DTO.displayName()),
            tuple(REQUESTER_USER_MERGE_FIELD_NAME, CONSULTEE_ALLOCATOR_1.displayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, consultationDeadline)
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(ENERGY_PORTAL_USER_DTO).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(applicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
}
