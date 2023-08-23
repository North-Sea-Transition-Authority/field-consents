package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneOffset.systemDefault());
  private static final Long WUA_ID = 1L;
  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().withWuaId(WUA_ID).build();
  private static final Team CONSULTATION_TEAM = new TeamTestUtil.TeamBuilder().build();

  @Mock
  private TeamService teamService;

  @Mock
  private ConsultationRepository repository;

  private ConsultationService consultationService;

  private ApplicationVersion applicationVersion;


  @BeforeEach
  void setUp() {
    consultationService = spy(new ConsultationService(teamService, repository, CLOCK));
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void openConsultationExistsForApplicationVersion(boolean exists) {
    when(repository.existsByRequestApplicationVersion(applicationVersion)).thenReturn(exists);
    assertThat(consultationService.openConsultationExistsForApplicationVersion(applicationVersion)).isEqualTo(exists);
  }

  @Test
  void requestConsultation() {
    doReturn(CONSULTATION_TEAM).when(consultationService).getConsultationTeam();

    var deadline = CLOCK.instant().plus(1, DAYS);
    consultationService.requestConsultation(applicationVersion, deadline, USER);

    var consultationCaptor = ArgumentCaptor.forClass(Consultation.class);
    verify(repository).save(consultationCaptor.capture());

    assertThat(consultationCaptor.getValue())
        .extracting(
            Consultation::getRequestApplicationVersion,
            Consultation::getRequestDeadline,
            Consultation::getConsultationTeam,
            Consultation::getRequestedAtDateTime,
            Consultation::getRequestedByWuaId
        ).containsExactly(
            applicationVersion,
            deadline,
            CONSULTATION_TEAM,
            CLOCK.instant(),
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

}
