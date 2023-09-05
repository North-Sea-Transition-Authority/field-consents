package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_REQUEST;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class ConsultationService {

  private static final TeamType CONSULTATION_TEAM_TYPE = TeamType.OPRED;

  private final TeamService teamService;
  private final ConsultationRepository repository;
  private final Clock clock;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  ConsultationService(
      TeamService teamService,
      ConsultationRepository repository,
      Clock clock,
      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService
  ) {
    this.teamService = teamService;
    this.repository = repository;
    this.clock = clock;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
  }

  public boolean openConsultationExistsForApplicationVersion(ApplicationVersion applicationVersion) {
    return repository.existsByRequestApplicationVersion(applicationVersion);
  }

  @Transactional
  public void requestConsultation(ApplicationVersion applicationVersion, Instant deadline, ServiceUserDetail user) {
    var consultation = new Consultation();
    consultation.setRequestApplicationVersion(applicationVersion);
    consultation.setRequestDeadline(deadline);
    consultation.setConsultationTeam(getConsultationTeam());

    consultation.setRequestedAtDatetime(clock.instant());
    consultation.setRequestedByWuaId(user.wuaId());

    repository.save(consultation);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        user,
        CONSULTATION_REQUEST,
        CONSULTEE
    );
  }

  public Team getConsultationTeam() {
    var teams = teamService.getTeamsByType(CONSULTATION_TEAM_TYPE);
    if (teams.size() == 1) {
      return teams.get(0);
    }

    throw new IllegalStateException("Expected exactly 1 team of type [%s]".formatted(CONSULTATION_TEAM_TYPE));
  }

}
