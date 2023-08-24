package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
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

  ConsultationService(
      TeamService teamService,
      ConsultationRepository repository,
      Clock clock
  ) {
    this.teamService = teamService;
    this.repository = repository;
    this.clock = clock;
  }

  public boolean openConsultationExistsForApplicationVersion(ApplicationVersion applicationVersion) {
    return repository.existsByRequestApplicationVersion(applicationVersion);
  }

  @Transactional
  public void requestConsultation(ApplicationVersion applicationVersion, Instant deadline, ServiceUserDetail userDetail) {
    var consultation = new Consultation();
    consultation.setRequestApplicationVersion(applicationVersion);
    consultation.setRequestDeadline(deadline);
    consultation.setConsultationTeam(getConsultationTeam());

    consultation.setRequestedAtDatetime(clock.instant());
    consultation.setRequestedByWuaId(userDetail.wuaId());

    repository.save(consultation);
  }

  public Team getConsultationTeam() {
    var teams = teamService.getTeamsByType(CONSULTATION_TEAM_TYPE);
    if (teams.size() == 1) {
      return teams.get(0);
    }

    throw new IllegalStateException("Expected exactly 1 team of type [%s]".formatted(CONSULTATION_TEAM_TYPE));
  }

}
