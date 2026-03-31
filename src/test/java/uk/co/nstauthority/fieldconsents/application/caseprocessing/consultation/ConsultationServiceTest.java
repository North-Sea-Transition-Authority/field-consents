package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus.CLOSED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_RESPONDER_ASSIGNMENT;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_RESPONSE;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

  private static final Long WUA_ID = 1L;
  private static final ServiceUserDetail RESPONDER_USER = ServiceUserDetailTestUtil.Builder().withWuaId(WUA_ID).build();
  private static final ServiceUserDetail ASSIGNER_USER = ServiceUserDetailTestUtil.Builder().withWuaId(WUA_ID + 1).build();
  private static final TeamType CONSULTATION_TEAM_TYPE = TeamType.CONSULTEE;
  private static final Integer CONSULTATION_ID = 1;
  private static final Team CONSULTATION_TEAM = TeamTestUtil.newBuilder().withTeamType(TeamType.CONSULTEE).build();
  private static final Long REQUESTER_USER_WUA_ID = 6L;
  private static final ServiceUserDetail REQUESTER_USER = ServiceUserDetailTestUtil.Builder().withWuaId(REQUESTER_USER_WUA_ID).build();

  @Mock
  private ConsultationRepository repository;

  @Mock(strictness = LENIENT)
  private Clock clock;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Mock
  private ConsultationEmailService consultationEmailService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private ConsultationService consultationService;

  @Captor
  private ArgumentCaptor<Consultation> consultationArgumentCaptor;

  private ApplicationVersion applicationVersion;

  private Application application;

  private Consultation consultation;

  @BeforeEach
  void setUp() {
    var now = Instant.now();
    when(clock.instant()).thenReturn(now);

    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    consultation = new Consultation();
    consultation.setConsultationTeam(CONSULTATION_TEAM);
  }

  @Test
  void getLatestOpenConsultation_exists() {
    when(repository.findByRequestApplicationVersion_ApplicationAndStatus(application, OPEN)).thenReturn(Optional.of(consultation));

    assertThat(consultationService.getLatestOpenConsultation(application)).isEqualTo(consultation);
  }

  @Test
  void getLatestOpenConsultation_doesNotExist() {
    when(repository.findByRequestApplicationVersion_ApplicationAndStatus(application, OPEN)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> consultationService.getLatestOpenConsultation(application))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Consultation not found for application [%s]".formatted(application.getId()));
  }

  @Test
  void getConsultationByIdAndApplication() {
    when(repository.findByIdAndRequestApplicationVersion_Application(CONSULTATION_ID, application)).thenReturn(Optional.of(consultation));
    assertThat(consultationService.getConsultationByIdAndApplication(CONSULTATION_ID, application)).isEqualTo(consultation);
  }

  @Test
  void getConsultationByIdAndApplication_doesNotExist() {
    var consultationId = 1;
    when(repository.findByIdAndRequestApplicationVersion_Application(consultationId, application)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> consultationService.getConsultationByIdAndApplication(consultationId, application))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Consultation [%s] not found for application [%s]".formatted(consultationId, application.getId()));
  }

  @Test
  void getConsultationsByApplication() {
    when(repository.findAllByRequestApplicationVersion_ApplicationOrderById(application)).thenReturn(Collections.singletonList(consultation));
    assertThat(consultationService.getConsultationsByApplication(application)).containsExactly(consultation);
  }

  @Test
  void getOpenConsultationByIdAndApplication() {
    when(repository.findByIdAndRequestApplicationVersion_ApplicationAndStatus(CONSULTATION_ID, application, OPEN)).thenReturn(Optional.of(consultation));
    assertThat(consultationService.getOpenConsultationByIdAndApplication(CONSULTATION_ID, application)).isEqualTo(consultation);
  }

  @Test
  void getOpenConsultationByIdAndApplication_doesNotExist() {
    var consultationId = 1;
    when(repository.findByIdAndRequestApplicationVersion_ApplicationAndStatus(consultationId, application, OPEN)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> consultationService.getOpenConsultationByIdAndApplication(consultationId, application))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Open consultation [%s] not found for application [%s]".formatted(consultationId, application.getId()));
  }

  @Test
  void getConsultationsByApplication_consultationsDontExist() {
    when(repository.findAllByRequestApplicationVersion_ApplicationOrderById(application)).thenReturn(Collections.emptyList());
    assertThat(consultationService.getConsultationsByApplication(application)).isEmpty();
  }

  @Test
  void getConsultationsByApplicationForUser_whenNoConsultationTeamFound() {
    when(teamQueryService.getStaticTeamRoles(REQUESTER_USER, TeamType.CONSULTEE))
        .thenReturn(Collections.emptyList());

    assertThat(consultationService.getConsultationsByApplicationForUser(application, REQUESTER_USER))
        .isEmpty();
  }

  @Test
  void getConsultationsByApplicationForUser_whenNoConsultationFoundForTeam() {
    when(teamQueryService.getStaticTeamRoles(REQUESTER_USER, TeamType.CONSULTEE))
        .thenReturn(List.of(
            TeamRoleTestUtil.newBuilder()
                .withTeam(CONSULTATION_TEAM)
                .build()
        ));

    when(repository.findAllByRequestApplicationVersion_ApplicationAndConsultationTeamInOrderById(application, List.of(CONSULTATION_TEAM)))
        .thenReturn(Collections.emptyList());

    assertThat(consultationService.getConsultationsByApplicationForUser(application, REQUESTER_USER))
        .isEmpty();
  }

  @Test
  void getConsultationsByApplicationForUser_whenOneConsultationFoundFromUserTeam() {
    when(teamQueryService.getStaticTeamRoles(REQUESTER_USER, TeamType.CONSULTEE))
        .thenReturn(List.of(
            TeamRoleTestUtil.newBuilder()
                .withTeam(CONSULTATION_TEAM)
                .build()
        ));

    when(repository.findAllByRequestApplicationVersion_ApplicationAndConsultationTeamInOrderById(application, List.of(CONSULTATION_TEAM)))
        .thenReturn(List.of(consultation));

    assertThat(consultationService.getConsultationsByApplicationForUser(application, REQUESTER_USER))
        .containsExactly(consultation);
  }

  @Test
  void requestConsultation() {
    when(teamQueryService.getStaticTeam(CONSULTATION_TEAM_TYPE)).thenReturn(CONSULTATION_TEAM);

    var deadline = clock.instant().plus(1, DAYS);
    consultationService.requestConsultation(applicationVersion, deadline, REQUESTER_USER);

    verify(repository).save(consultationArgumentCaptor.capture());
    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, REQUESTER_USER, CONSULTATION_REQUEST, CONSULTEE);

    var actualConsultation = consultationArgumentCaptor.getValue();
    assertThat(actualConsultation)
        .extracting(
            Consultation::getRequestApplicationVersion,
            Consultation::getStatus,
            Consultation::getRequestDeadline,
            Consultation::getConsultationTeam,
            Consultation::getRequestedAtDatetime,
            Consultation::getRequestedByWuaId
        ).containsExactly(
            applicationVersion,
            OPEN,
            deadline,
            CONSULTATION_TEAM,
            clock.instant(),
            REQUESTER_USER_WUA_ID
        );

    verify(consultationEmailService).sendConsultationRequestEmail(actualConsultation);
  }

  @Test
  void requestConsultation_whenSendConsultationRequestEmailFails_thenConsultationRequestIsStillSubmitted() {
    when(teamQueryService.getStaticTeam(CONSULTATION_TEAM_TYPE)).thenReturn(CONSULTATION_TEAM);

    var deadline = clock.instant().plus(1, DAYS);

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(consultationEmailService).sendConsultationRequestEmail(consultation);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> consultationService.requestConsultation(applicationVersion, deadline, REQUESTER_USER)
    );

    verify(repository).save(consultationArgumentCaptor.capture());
    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, REQUESTER_USER, CONSULTATION_REQUEST, CONSULTEE);

    var actualConsultation = consultationArgumentCaptor.getValue();
    assertThat(actualConsultation)
        .extracting(
            Consultation::getRequestApplicationVersion,
            Consultation::getStatus,
            Consultation::getRequestDeadline,
            Consultation::getConsultationTeam,
            Consultation::getRequestedAtDatetime,
            Consultation::getRequestedByWuaId
        ).containsExactly(
            applicationVersion,
            OPEN,
            deadline,
            CONSULTATION_TEAM,
            clock.instant(),
            REQUESTER_USER_WUA_ID
        );

    verify(consultationEmailService).sendConsultationRequestEmail(actualConsultation);
  }

  @Test
  void getConsultationTeam() {
    when(teamQueryService.getStaticTeam(CONSULTATION_TEAM_TYPE)).thenReturn(CONSULTATION_TEAM);
    assertThat(consultationService.getConsultationTeam()).isEqualTo(CONSULTATION_TEAM);
  }

  @Test
  void getAllAvailableConsultationResponders_isResponderInTeam() {
    var responder =TeamRoleTestUtil.newBuilder()
        .withTeam(CONSULTATION_TEAM)
        .withRole(Role.RESPONDER)
        .build();

    var teamRoles = List.of(
        responder, // this is the one we care about
        TeamRoleTestUtil.newBuilder()
            .withTeam(CONSULTATION_TEAM)
            .withRole(Role.ACCESS_MANAGER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(CONSULTATION_TEAM)
            .withRole(Role.ALLOCATOR)
            .build()
    );

    when(teamQueryService.getTeamRoles(CONSULTATION_TEAM)).thenReturn(teamRoles);

    var teamMemberView = mock(TeamMemberView.class);

    when(teamQueryService.getTeamMemberViews(List.of(responder))).thenReturn(List.of(teamMemberView));

    assertThat(consultationService.getAllAvailableConsultationRespondersForConsultation(consultation))
        .containsExactly(teamMemberView);
  }

  @Test
  void getAllAvailableConsultationResponders_isNotResponderInTeam() {
    when(teamQueryService.getTeamRoles(CONSULTATION_TEAM))
        .thenReturn(List.of(
            TeamRoleTestUtil.newBuilder()
                .withTeam(CONSULTATION_TEAM)
                .withRole(Role.ACCESS_MANAGER)
                .build(),
            TeamRoleTestUtil.newBuilder()
                .withTeam(CONSULTATION_TEAM)
                .withRole(Role.ALLOCATOR)
                .build()
        ));

    when(teamQueryService.getTeamMemberViews(List.of())).thenReturn(List.of());

    assertThat(consultationService.getAllAvailableConsultationRespondersForConsultation(consultation)).isEmpty();
  }

  @Test
  void assignResponderToConsultation() {
    var consultation = mock(Consultation.class);
    when(consultation.getRequestApplicationVersion()).thenReturn(applicationVersion);

    when(teamQueryService.userHasStaticRole(RESPONDER_USER, TeamType.CONSULTEE, Role.RESPONDER)).thenReturn(true);

    consultationService.assignResponderToConsultation(consultation, ASSIGNER_USER, RESPONDER_USER);

    verify(consultation).setResponderWuaId(RESPONDER_USER.wuaId());
    verify(consultation).getRequestApplicationVersion();
    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, ASSIGNER_USER, CONSULTATION_RESPONDER_ASSIGNMENT, CONSULTEE);
    verifyNoMoreInteractions(consultation);
    verify(repository).save(consultation);
    verify(consultationEmailService).sendConsultationAssignmentEmail(consultation, ASSIGNER_USER);
  }

  @Test
  void assignResponderToConsultation_whenSendConsultationAssignmentEmailFails_thenConsultationIsStillAssigned() {
    var consultation = mock(Consultation.class);
    when(consultation.getRequestApplicationVersion()).thenReturn(applicationVersion);

    when(teamQueryService.userHasStaticRole(RESPONDER_USER, TeamType.CONSULTEE, Role.RESPONDER)).thenReturn(true);

    // WHEN the email service call throws an exception
    doThrow(new RuntimeException("Failed to send email"))
        .when(consultationEmailService).sendConsultationAssignmentEmail(consultation, ASSIGNER_USER);

    // THEN it will be caught by the caller and not re-thrown
    assertDoesNotThrow(
        () -> consultationService.assignResponderToConsultation(consultation, ASSIGNER_USER, RESPONDER_USER)
    );

    verify(consultation).setResponderWuaId(RESPONDER_USER.wuaId());
    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, ASSIGNER_USER, CONSULTATION_RESPONDER_ASSIGNMENT, CONSULTEE);
    verifyNoMoreInteractions(consultation);
    verify(repository).save(consultation);
    verify(consultationEmailService).sendConsultationAssignmentEmail(consultation, ASSIGNER_USER);
  }

  @Test
  void assignResponderToConsultation_notResponderForTeam() {
    var consultation = mock(Consultation.class);

    when(teamQueryService.userHasStaticRole(RESPONDER_USER, TeamType.CONSULTEE, Role.RESPONDER)).thenReturn(false);

    assertThatThrownBy(() -> consultationService.assignResponderToConsultation(consultation, ASSIGNER_USER, RESPONDER_USER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User [%d] is not a %s in a %s team".formatted(
            RESPONDER_USER.wuaId(),
            Role.RESPONDER,
            TeamType.CONSULTEE
        ));

    verify(consultationEmailService, never()).sendConsultationAssignmentEmail(consultation, ASSIGNER_USER);
  }

  @Test
  void saveConsultationResponse() {
    var habitatsRegsResponseType = HabitatsRegsResponseType.AGREE;
    var habitatsRegsDescription = "habitats description";
    var eiaRegsResponseType = EiaRegsResponseType.AGREE;
    var eiaRegsDescription = "eia description";

    var documents = Collections.<UploadedFileForm>emptyList();

    var consultation = mock(Consultation.class);
    when(consultation.getId()).thenReturn(CONSULTATION_ID);
    when(consultation.getRequestApplicationVersion()).thenReturn(applicationVersion);

    when(repository.save(consultation)).thenReturn(consultation);

    consultationService.saveConsultationResponse(
        applicationVersion,
        consultation,
        RESPONDER_USER,
        habitatsRegsResponseType,
        habitatsRegsDescription,
        eiaRegsResponseType,
        eiaRegsDescription,
        documents
    );

    verify(consultation).setStatus(CLOSED);
    verify(consultation).setResponseApplicationVersion(applicationVersion);
    verify(consultation).setRespondedByWuaId(RESPONDER_USER.wuaId());
    verify(consultation).setRespondedAtDatetime(clock.instant());
    verify(consultation).setHabitatsRegsResponseType(habitatsRegsResponseType);
    verify(consultation).setHabitatsRegsResponseDescription(habitatsRegsDescription);
    verify(consultation).setEiaRegsResponseType(eiaRegsResponseType);
    verify(consultation).setEiaRegsResponseDescription(eiaRegsDescription);
    verify(repository).save(consultation);

    verify(consultation).getId();

    var fileUsageCaptor = ArgumentCaptor.forClass(ConsultationFileUsage.class);
    verify(fieldConsentsFileService).saveDocuments(fileUsageCaptor.capture(), eq(documents));
    assertThat(fileUsageCaptor.getValue()).extracting(
        ConsultationFileUsage::usageId,
        ConsultationFileUsage::usageType,
        ConsultationFileUsage::documentType
    ).containsExactly(
        CONSULTATION_ID.toString(),
        "Consultation",
        "response-document"
    );

    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(
        applicationVersion,
        RESPONDER_USER,
        CONSULTATION_RESPONSE,
        REGULATOR
    );

    verifyNoMoreInteractions(consultation);
  }

  @Test
  void requiresEiaResponse_production() {
    var applicationVersion = mock(ApplicationVersion.class);
    var application = mock(Application.class);

    when(applicationVersion.getApplication()).thenReturn(application);
    when(application.getType()).thenReturn(ApplicationType.PRODUCTION);

    assertThat(consultationService.requiresEiaRegsResponse(applicationVersion)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EXCLUDE)
  void requiresEiaResponse_notProduction(ApplicationType applicationType) {
    var applicationVersion = mock(ApplicationVersion.class);
    var application = mock(Application.class);

    when(applicationVersion.getApplication()).thenReturn(application);
    when(application.getType()).thenReturn(applicationType);

    assertThat(consultationService.requiresEiaRegsResponse(applicationVersion)).isFalse();
  }

  @Test
  void findAllOpenConsultations() {
    var expected = List.of(
        mock(Consultation.class)
    );

    when(repository.findAllByStatus(ConsultationStatus.OPEN))
        .thenReturn(expected);

    var result = consultationService.findAllOpenConsultations();

    assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    verify(repository).findAllByStatus(ConsultationStatus.OPEN);
  }

}
