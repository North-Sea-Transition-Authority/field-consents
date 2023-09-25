package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_REQUEST;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
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
  void getConsultationsByApplication() {
    when(repository.findAllByRequestApplicationVersion_ApplicationOrderById(application)).thenReturn(Collections.singletonList(consultation));
    assertThat(consultationService.getConsultationsByApplication(application)).containsExactly(consultation);
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
    verify(applicationWorkAreaPriorityService).prioritiseApplicationInWorkArea(applicationVersion, ASSIGNER_USER, CONSULTATION_REQUEST, CONSULTEE);
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

}
