package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationEmailService.CASE_MANAGERS_RECIPIENT_DISPLAY_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationEmailService.CONSULTATION_AGREE_DECISION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationEmailService.CONSULTATION_DOES_NOT_AGREE_DECISION;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationServiceTest.CONSULTATION_TEAM;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.APPLICATION_VERSION_DOMAIN_REFERENCE;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_MANAGER_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_MANAGER_2;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.CASE_OFFICER_EPU;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.TEAM_MEMBER_VIEW_CASE_MANAGER_1;
import static uk.co.nstauthority.fieldconsents.email.EmailMergeFieldTestUtil.TEAM_MEMBER_VIEW_CASE_MANAGER_2;
import static uk.co.nstauthority.fieldconsents.email.EmailService.RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUESTER_USER_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.email.EmailService.REQUEST_DEADLINE_MERGE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;
import static uk.co.nstauthority.fieldconsents.integrationtest.ApplicationDataItemViewIntegrationTestUtil.ENERGY_PORTAL_USER_DTO;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

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

  private ApplicationVersion productionApplicationVersion;

  private Consultation productionConsultation;

  private Consultation flareConsultation;

  private String consultationDeadline;
  
  @BeforeEach
  void setUp() {
    productionApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var flareApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);

    var requestDeadlineInstant = Instant.now();
    consultationDeadline = DateUtils.format(requestDeadlineInstant, DATE_TIME);
    productionConsultation = new Consultation();
    productionConsultation.setConsultationTeam(CONSULTATION_TEAM);
    productionConsultation.setRequestDeadline(requestDeadlineInstant);
    productionConsultation.setRequestApplicationVersion(productionApplicationVersion);
    productionConsultation.setResponderWuaId(ENERGY_PORTAL_USER_DTO.webUserAccountId());

    flareConsultation = new Consultation();
    flareConsultation.setConsultationTeam(CONSULTATION_TEAM);
    flareConsultation.setRequestDeadline(requestDeadlineInstant);
    flareConsultation.setRequestApplicationVersion(flareApplicationVersion);
    flareConsultation.setResponderWuaId(ENERGY_PORTAL_USER_DTO.webUserAccountId());

    consultationEmailService = new ConsultationEmailService(emailService, teamMemberViewService, energyPortalUserService);
  }

  @Test
  void sendConsultationRequestEmail_withNoConsulteeAllocatorsToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUEST, productionApplicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(productionConsultation.getConsultationTeam(), Set.of(OpredTeamRole.ALLOCATOR)))
        .thenReturn(Collections.emptyList());

    consultationEmailService.sendConsultationRequestEmail(productionConsultation);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendConsultationRequestEmail_withOneConsulteeAllocatorToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUEST, productionApplicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(productionConsultation.getConsultationTeam(), Set.of(OpredTeamRole.ALLOCATOR)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSULTEE_ALLOCATOR_1));

    consultationEmailService.sendConsultationRequestEmail(productionConsultation);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, productionConsultation.getConsultationTeam().getDisplayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, consultationDeadline)
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CONSULTEE_ALLOCATOR_1).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(productionApplicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsultationRequestEmail_withMultipleConsulteeAllocatorsToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_REQUEST, productionApplicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeam(productionConsultation.getConsultationTeam(), Set.of(OpredTeamRole.ALLOCATOR)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CONSULTEE_ALLOCATOR_1, TEAM_MEMBER_VIEW_CONSULTEE_ALLOCATOR_2));

    consultationEmailService.sendConsultationRequestEmail(productionConsultation);

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
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, productionConsultation.getConsultationTeam().getDisplayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, consultationDeadline)
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, productionConsultation.getConsultationTeam().getDisplayName()),
            tuple(REQUEST_DEADLINE_MERGE_FIELD_NAME, consultationDeadline)
        );

    // verify email recipients
    var testEmailRecipients = emailRecipientCaptor.getAllValues();
    assertThat(testEmailRecipients).hasSize(2);

    assertThat(testEmailRecipients.get(0).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CONSULTEE_ALLOCATOR_1).getEmailAddress());
    assertThat(testEmailRecipients.get(1).getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CONSULTEE_ALLOCATOR_2).getEmailAddress());

    // verify domain references
    assertThat(domainReferences.get(0).getDomainId())
        .isEqualTo(productionApplicationVersion.getId().toString());
    assertThat(domainReferences.get(1).getDomainId())
        .isEqualTo(productionApplicationVersion.getId().toString());

    assertThat(domainReferences.get(0).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
    assertThat(domainReferences.get(1).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }
  
  @Test
  void sendConsultationAssignmentEmail() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_ASSIGNMENT, productionApplicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(energyPortalUserService.getByWuaId(any())).thenReturn(ENERGY_PORTAL_USER_DTO);

    consultationEmailService.sendConsultationAssignmentEmail(productionConsultation, CONSULTEE_ALLOCATOR_1);

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
        .isEqualTo(productionApplicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsultationResponseEmail_whenCaseOfficerIsCurrentOwner() {
    productionApplicationVersion.setCaseOfficerWuaId(CASE_OFFICER.wuaId());

    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_RESPONSE, productionApplicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));
    when(energyPortalUserService.getByWuaId(any())).thenReturn(CASE_OFFICER_EPU);

    consultationEmailService.sendConsultationResponseEmail(productionConsultation);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_OFFICER_EPU.displayName()),
            tuple("CONSULTEE_NAME", productionConsultation.getConsultationTeam().getDisplayName()),
            tuple("CONSULTATION_DECISION", CONSULTATION_AGREE_DECISION)
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_OFFICER_EPU).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(productionApplicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsultationResponseEmail_whenCaseOfficerIsNotAssigned_withNoCaseManagersToNotify() {
    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_RESPONSE, productionApplicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(Collections.emptyList());

    consultationEmailService.sendConsultationResponseEmail(productionConsultation);

    verify(emailService, never()).sendEmail(any(), any(), any());
  }

  @Test
  void sendConsultationResponseEmail_whenCaseOfficerIsNotAssigned_withOneCaseManagerToNotify() {

    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_RESPONSE, productionApplicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CASE_MANAGER_1));

    consultationEmailService.sendConsultationResponseEmail(productionConsultation);

    verify(emailService).sendEmail(
        templateCaptor.capture(),
        emailRecipientCaptor.capture(),
        domainReferenceCaptor.capture()
    );

    assertThat(templateCaptor.getValue().getMailMergeFields())
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME),
            tuple("CONSULTEE_NAME", productionConsultation.getConsultationTeam().getDisplayName()),
            tuple("CONSULTATION_DECISION", CONSULTATION_AGREE_DECISION)
        );

    assertThat(emailRecipientCaptor.getValue().getEmailAddress())
        .isEqualTo(FieldConsentsEmailRecipient.from(CASE_MANAGER_1).getEmailAddress());

    assertThat(domainReferenceCaptor.getValue().getDomainId())
        .isEqualTo(productionApplicationVersion.getId().toString());

    assertThat(domainReferenceCaptor.getValue().getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void sendConsultationResponseEmail_whenCaseOfficerIsNotAssigned_withMultipleCaseManagersToNotify() {

    when(emailService.getTemplate(GovukNotifyTemplate.CONSULTATION_RESPONSE, productionApplicationVersion))
        .thenReturn(MergedTemplate.builder(new Template(null, null, Set.of(), null)));

    when(teamMemberViewService
        .getTeamMemberViewsWithRolesForTeamType(TeamType.REGULATOR, Set.of(RegulatorTeamRole.CASE_MANAGER)))
        .thenReturn(List.of(TEAM_MEMBER_VIEW_CASE_MANAGER_1, TEAM_MEMBER_VIEW_CASE_MANAGER_2));

    consultationEmailService.sendConsultationResponseEmail(productionConsultation);

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
            tuple("CONSULTEE_NAME", productionConsultation.getConsultationTeam().getDisplayName()),
            tuple("CONSULTATION_DECISION", CONSULTATION_AGREE_DECISION)
        );

    var secondEmailMergeFields = emailTemplates.get(1).getMailMergeFields();
    assertThat(secondEmailMergeFields)
        .extracting(MailMergeField::name, MailMergeField::value)
        .containsOnly(
            tuple(RECIPIENT_IDENTIFIER_MERGE_FIELD_NAME, CASE_MANAGERS_RECIPIENT_DISPLAY_NAME),
            tuple("CONSULTEE_NAME", productionConsultation.getConsultationTeam().getDisplayName()),
            tuple("CONSULTATION_DECISION", CONSULTATION_AGREE_DECISION)
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
        .isEqualTo(productionApplicationVersion.getId().toString());
    assertThat(domainReferences.get(1).getDomainId())
        .isEqualTo(productionApplicationVersion.getId().toString());

    assertThat(domainReferences.get(0).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
    assertThat(domainReferences.get(1).getDomainType())
        .isEqualTo(APPLICATION_VERSION_DOMAIN_REFERENCE);
  }

  @Test
  void getConsultationDecision_whenConsulteeDisagreesToHabitatsRegs_thenDoesNotAgreeDecision() {
    flareConsultation.setHabitatsRegsResponseType(HabitatsRegsResponseType.DO_NOT_AGREE);

    assertThat(consultationEmailService.getConsultationDecision(flareConsultation))
        .isEqualTo(CONSULTATION_DOES_NOT_AGREE_DECISION);
  }

  @ParameterizedTest
  @EnumSource(value = HabitatsRegsResponseType.class, names = { "AGREE", "DOES_NOT_APPLY" })
  void getConsultationDecision_whenConsulteeAgreesOrDoesNotApplyHabitatsRegsOnly_thenAgreeDecision(HabitatsRegsResponseType habitatsRegsResponseType) {
    flareConsultation.setHabitatsRegsResponseType(habitatsRegsResponseType);

    assertThat(consultationEmailService.getConsultationDecision(flareConsultation))
        .isEqualTo(CONSULTATION_AGREE_DECISION);
  }

  @ParameterizedTest
  @MethodSource("getHabitatsEiaRegulations_withDoesNotAgreeDecision")
  void getConsultationDecision_whenConsulteeDisagreesToEitherHabitatsOrEiaRegulations_thenDoesNotAgreeDecision(
      HabitatsRegsResponseType habitatsRegsResponseType,
      EiaRegsResponseType eiaRegsResponseType) {
    productionConsultation.setHabitatsRegsResponseType(habitatsRegsResponseType);
    productionConsultation.setEiaRegsResponseType(eiaRegsResponseType);

    assertThat(consultationEmailService.getConsultationDecision(productionConsultation))
        .isEqualTo(CONSULTATION_DOES_NOT_AGREE_DECISION);
  }

  @ParameterizedTest
  @MethodSource("getHabitatsEiaRegulations_withAgreeDecision")
  void getConsultationDecision_whenConsulteeAgreesOrDoesNotApplyEitherHabitatsOrEiaRegulations_thenAgreeDecision(
      HabitatsRegsResponseType habitatsRegsResponseType,
      EiaRegsResponseType eiaRegsResponseType) {
    productionConsultation.setHabitatsRegsResponseType(habitatsRegsResponseType);
    productionConsultation.setEiaRegsResponseType(eiaRegsResponseType);

    assertThat(consultationEmailService.getConsultationDecision(productionConsultation))
        .isEqualTo(CONSULTATION_AGREE_DECISION);
  }

  private static Stream<Arguments> getHabitatsEiaRegulations_withDoesNotAgreeDecision() {
    return Stream.of(
        Arguments.of(HabitatsRegsResponseType.DO_NOT_AGREE, EiaRegsResponseType.DO_NOT_AGREE),
        Arguments.of(HabitatsRegsResponseType.DO_NOT_AGREE, EiaRegsResponseType.AGREE),
        Arguments.of(HabitatsRegsResponseType.DO_NOT_AGREE, EiaRegsResponseType.DOES_NOT_APPLY),
        Arguments.of(HabitatsRegsResponseType.AGREE, EiaRegsResponseType.DO_NOT_AGREE),
        Arguments.of(HabitatsRegsResponseType.DOES_NOT_APPLY, EiaRegsResponseType.DO_NOT_AGREE)
    );
  }

  private static Stream<Arguments> getHabitatsEiaRegulations_withAgreeDecision() {
    return Stream.of(
        Arguments.of(HabitatsRegsResponseType.AGREE, EiaRegsResponseType.DOES_NOT_APPLY),
        Arguments.of(HabitatsRegsResponseType.AGREE, EiaRegsResponseType.AGREE),
        Arguments.of(HabitatsRegsResponseType.DOES_NOT_APPLY, EiaRegsResponseType.AGREE)
    );
  }
}
