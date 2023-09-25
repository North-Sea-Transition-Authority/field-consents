package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_REQUEST;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamId;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamRole;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.opred.OpredTeamService;

@Service
public class ConsultationService {

  private static final TeamType CONSULTATION_TEAM_TYPE = TeamType.OPRED;

  private final TeamService teamService;
  private final ConsultationRepository repository;
  private final Clock clock;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;
  private final OpredTeamService opredTeamService;
  private final TeamMemberViewService teamMemberViewService;

  ConsultationService(
      TeamService teamService,
      ConsultationRepository repository,
      Clock clock,
      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
      OpredTeamService opredTeamService,
      TeamMemberViewService teamMemberViewService
  ) {
    this.teamService = teamService;
    this.repository = repository;
    this.clock = clock;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.opredTeamService = opredTeamService;
    this.teamMemberViewService = teamMemberViewService;
  }

  public Optional<Consultation> findLatestOpenConsultation(Application application) {
    return repository.findByRequestApplicationVersion_ApplicationAndStatus(application, OPEN);
  }

  public Consultation getLatestOpenConsultation(Application application) {
    return findLatestOpenConsultation(application).orElseThrow(() -> new EntityNotFoundException(
        "Consultation not found for application [%s]".formatted(application.getId())
    ));
  }

  public List<Consultation> getConsultationsByApplication(Application application) {
    return repository.findAllByRequestApplicationVersion_ApplicationOrderById(application);
  }

  @Transactional
  public void requestConsultation(ApplicationVersion applicationVersion, Instant deadline, ServiceUserDetail user) {
    var consultation = new Consultation();
    consultation.setStatus(OPEN);
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

  public List<TeamMemberView> getAllAvailableConsultationRespondersForConsultation(Consultation consultation) {
    return teamMemberViewService.getTeamMemberViewsForTeam(consultation.getConsultationTeam())
        .stream()
        .filter(tmv -> tmv.teamRoles().contains(OpredTeamRole.RESPONDER))
        .sorted(Comparator.comparing(TeamMemberView::getDisplayName))
        .toList();
  }

  @Transactional
  public void assignResponderToConsultation(Consultation consultation,
                                            ServiceUserDetail assigner,
                                            ServiceUserDetail responder) {
    var consultationTeamId = TeamId.valueOf(consultation.getConsultationTeam());
    if (!opredTeamService.isResponder(consultationTeamId, responder)) {
      throw new IllegalArgumentException("Responder must be a member of team [%s]".formatted(consultationTeamId.id()));
    }

    consultation.setResponderWuaId(responder.wuaId());
    repository.save(consultation);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        consultation.getRequestApplicationVersion(),
        assigner,
        CONSULTATION_REQUEST,
        CONSULTEE
    );
  }

}
