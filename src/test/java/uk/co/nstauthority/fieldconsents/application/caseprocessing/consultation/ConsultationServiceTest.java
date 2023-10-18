package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;
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
import java.util.Set;
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
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.TeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamService;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

  private static final Long WUA_ID = 1L;
  private static final ServiceUserDetail RESPONDER_USER = ServiceUserDetailTestUtil.Builder().withWuaId(WUA_ID).build();
  private static final ServiceUserDetail ASSIGNER_USER = ServiceUserDetailTestUtil.Builder().withWuaId(WUA_ID + 1).build();
  private static final TeamType CONSULTATION_TEAM_TYPE = TeamType.OPRED;
  private static final Integer CONSULTATION_ID = 1;
  private static final Team CONSULTATION_TEAM = new TeamTestUtil.TeamBuilder()
      .withId(1)
      .withTeamType(TeamType.OPRED)
      .build();

  @Mock
  private TeamService teamService;

  @Mock
  private ConsultationRepository repository;

  @Mock(strictness = LENIENT)
  private Clock clock;

  @Mock
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Mock
  private OpredTeamService opredTeamService;

  @Mock
  private TeamMemberViewService teamMemberViewService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @InjectMocks
  private ConsultationService consultationService;

  @Captor
  private ArgumentCaptor<Consultation> consultationArgumentCaptor;

  private ApplicationVersion applicationVersion;

  private Application application;

  private Consultation consultation;

  private TeamMemberView teamMemberView;

  @BeforeEach
  void setUp() {
    var now = Instant.now();
    when(clock.instant()).thenReturn(now);

    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    consultation = new Consultation();
    consultation.setConsultationTeam(CONSULTATION_TEAM);
  }

  private void setTeamMemberViewWithRoles(Set<TeamRole> teamRoles) {
    teamMemberView = new TeamMemberView(
        WebUserAccountId.from(RESPONDER_USER),
        new TeamView(CONSULTATION_TEAM.toTeamId(), CONSULTATION_TEAM_TYPE, "Consultation team"),
        "Mr",
        "Consultation",
        "Responder",
        "consultation.responder@test-team.com",
        "0123",
        teamRoles
    );
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
  void requestConsultation() {
    when(teamService.getTeamsByType(CONSULTATION_TEAM_TYPE)).thenReturn(Collections.singletonList(CONSULTATION_TEAM));

    var deadline = clock.instant().plus(1, DAYS);
    consultationService.requestConsultation(applicationVersion, deadline, RESPONDER_USER);

    verify(repository).save(consultationArgumentCaptor.capture());
    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, RESPONDER_USER, CONSULTATION_REQUEST, CONSULTEE);

    assertThat(consultationArgumentCaptor.getValue())
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
            WUA_ID
        );
  }

  @Test
  void getConsultationTeam() {
    when(teamService.getTeamsByType(TeamType.OPRED)).thenReturn(Collections.singletonList(CONSULTATION_TEAM));
    assertThat(consultationService.getConsultationTeam()).isEqualTo(CONSULTATION_TEAM);
  }

  @Test
  void getConsultationTeam_multipleTeamsFound() {
    when(teamService.getTeamsByType(TeamType.OPRED)).thenReturn(List.of(CONSULTATION_TEAM, CONSULTATION_TEAM));
    assertThatThrownBy(() -> consultationService.getConsultationTeam())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Expected exactly 1 team of type [OPRED]");
  }

  @Test
  void getConsultationTeam_noTeamsFound() {
    when(teamService.getTeamsByType(TeamType.OPRED)).thenReturn(Collections.emptyList());
    assertThatThrownBy(() -> consultationService.getConsultationTeam())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Expected exactly 1 team of type [OPRED]");
  }

  @Test
  void getAllAvailableConsultationResponders_isResponderInTeam() {
    setTeamMemberViewWithRoles(Collections.singleton(OpredTeamRole.RESPONDER));

    when(teamMemberViewService.getTeamMemberViewsForTeam(CONSULTATION_TEAM))
        .thenReturn(Collections.singletonList(teamMemberView));

    assertThat(consultationService.getAllAvailableConsultationRespondersForConsultation(consultation))
        .containsExactly(teamMemberView);
  }

  @Test
  void getAllAvailableConsultationResponders_isResponderAndAllocatorInTeam() {
    setTeamMemberViewWithRoles(Set.of(OpredTeamRole.RESPONDER, OpredTeamRole.ALLOCATOR));

    when(teamMemberViewService.getTeamMemberViewsForTeam(CONSULTATION_TEAM))
        .thenReturn(Collections.singletonList(teamMemberView));

    assertThat(consultationService.getAllAvailableConsultationRespondersForConsultation(consultation))
        .containsExactly(teamMemberView);
  }

  @Test
  void getAllAvailableConsultationResponders_isNotResponderInTeam() {
    setTeamMemberViewWithRoles(Collections.emptySet());

    when(teamMemberViewService.getTeamMemberViewsForTeam(CONSULTATION_TEAM))
        .thenReturn(Collections.singletonList(teamMemberView));

    assertThat(consultationService.getAllAvailableConsultationRespondersForConsultation(consultation)).isEmpty();
  }

  @Test
  void getAllAvailableConsultationResponders_consultationTeamIsDifferent() {
    var correctTeamId = CONSULTATION_TEAM.getId();
    var differentTeam = TeamTestUtil.Builder()
        .withId(correctTeamId + 1) // make it incorrect
        .build();
    consultation.setConsultationTeam(differentTeam);

    setTeamMemberViewWithRoles(Collections.singleton(OpredTeamRole.RESPONDER));

    when(teamMemberViewService.getTeamMemberViewsForTeam(differentTeam)).thenReturn(Collections.emptyList());

    assertThat(consultationService.getAllAvailableConsultationRespondersForConsultation(consultation)).isEmpty();
  }

  @Test
  void assignResponderToConsultation() {
    var consultation = mock(Consultation.class);
    when(consultation.getRequestApplicationVersion()).thenReturn(applicationVersion);
    when(consultation.getConsultationTeam()).thenReturn(CONSULTATION_TEAM);

    var consultationTeamId = TeamId.valueOf(consultation.getConsultationTeam());
    when(opredTeamService.isResponder(consultationTeamId, RESPONDER_USER)).thenReturn(true);

    consultationService.assignResponderToConsultation(consultation, ASSIGNER_USER, RESPONDER_USER);

    verify(consultation).setResponderWuaId(RESPONDER_USER.wuaId());
    verify(consultation).getRequestApplicationVersion();
    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, ASSIGNER_USER, CONSULTATION_RESPONDER_ASSIGNMENT, CONSULTEE);
    verifyNoMoreInteractions(consultation);
    verify(repository).save(consultation);
  }

  @Test
  void assignResponderToConsultation_notResponderForTeam() {
    var consultation = mock(Consultation.class);
    when(consultation.getConsultationTeam()).thenReturn(CONSULTATION_TEAM);

    var consultationTeamId = TeamId.valueOf(consultation.getConsultationTeam());
    when(opredTeamService.isResponder(consultationTeamId, RESPONDER_USER)).thenReturn(false);

    assertThatThrownBy(() -> consultationService.assignResponderToConsultation(consultation, ASSIGNER_USER,RESPONDER_USER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Responder must be a member of team [%s]".formatted(consultation.getConsultationTeam().getId()));
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

}
