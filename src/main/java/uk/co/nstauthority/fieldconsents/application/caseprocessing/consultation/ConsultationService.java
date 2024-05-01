package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus.CLOSED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_RESPONDER_ASSIGNMENT;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_RESPONSE;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
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

  public static final TeamType CONSULTATION_TEAM_TYPE = TeamType.OPRED;
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsultationService.class);

  private final TeamService teamService;
  private final ConsultationRepository repository;
  private final Clock clock;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;
  private final OpredTeamService opredTeamService;
  private final TeamMemberViewService teamMemberViewService;
  private final FieldConsentsFileService fieldConsentsFileService;
  private final ConsultationEmailService consultationEmailService;

  ConsultationService(
      TeamService teamService,
      ConsultationRepository repository,
      Clock clock,
      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
      OpredTeamService opredTeamService,
      TeamMemberViewService teamMemberViewService,
      FieldConsentsFileService fieldConsentsFileService,
      ConsultationEmailService consultationEmailService
  ) {
    this.teamService = teamService;
    this.repository = repository;
    this.clock = clock;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.opredTeamService = opredTeamService;
    this.teamMemberViewService = teamMemberViewService;
    this.fieldConsentsFileService = fieldConsentsFileService;
    this.consultationEmailService = consultationEmailService;
  }

  public Optional<Consultation> findLatestOpenConsultation(Application application) {
    return repository.findByRequestApplicationVersion_ApplicationAndStatus(application, OPEN);
  }

  public Consultation getLatestOpenConsultation(Application application) {
    return findLatestOpenConsultation(application).orElseThrow(() -> new EntityNotFoundException(
        "Consultation not found for application [%s]".formatted(application.getId())
    ));
  }

  public Consultation getConsultationByIdAndApplication(Integer consultationId, Application application) {
    return repository.findByIdAndRequestApplicationVersion_Application(consultationId, application)
        .orElseThrow(() -> new EntityNotFoundException("Consultation [%s] not found for application [%s]".formatted(
            consultationId, application.getId()
        )));
  }

  public Consultation getOpenConsultationByIdAndApplication(Integer consultationId, Application application) {
    return repository.findByIdAndRequestApplicationVersion_ApplicationAndStatus(consultationId, application, OPEN)
        .orElseThrow(() -> new EntityNotFoundException("Open consultation [%s] not found for application [%s]".formatted(
            consultationId, application.getId()
        )));
  }

  public List<Consultation> getConsultationsByApplication(Application application) {
    return repository.findAllByRequestApplicationVersion_ApplicationOrderById(application);
  }

  public Set<Consultation> getConsultationsByApplicationForUser(Application application, ServiceUserDetail user) {
    var teams = teamService.getTeamsOfTypeThatUserBelongsTo(user, CONSULTATION_TEAM_TYPE);
    var consultations = new HashSet<Consultation>();

    teams.forEach(team ->
        consultations.addAll(
            repository.findAllByRequestApplicationVersion_ApplicationAndConsultationTeamOrderById(application, team)));

    return consultations;
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

    try {
      consultationEmailService.sendConsultationRequestEmail(consultation);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a consultation request notification by user with wuaId [{}] for application \
              version with id [{}] failed. \
              Note: this hasn't prevented the consultation request being saved.
              """,
          user.wuaId(), applicationVersion.getId(), exception);
    }
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
        CONSULTATION_RESPONDER_ASSIGNMENT,
        CONSULTEE
    );

    try {
      consultationEmailService.sendConsultationAssignmentEmail(consultation, assigner);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a consultation assignment notification to the consultee responder by user with wuaId [{}]
              for application version with id [{}] failed. \
              Note: this hasn't prevented the assignment of the consultation to the consultee responder.
              """,
          assigner.wuaId(), consultation.getRequestApplicationVersion().getId(), exception);
    }
  }

  @Transactional
  public void saveConsultationResponse(
      ApplicationVersion applicationVersion,
      Consultation consultation,
      ServiceUserDetail responderUser,
      HabitatsRegsResponseType habitatsRegsResponseType,
      String habitatsRegsResponseDescription,
      EiaRegsResponseType eiaRegsResponseType,
      String eiaRegsResponseDescription,
      List<UploadedFileForm> documents
  ) {
    consultation.setStatus(CLOSED);
    consultation.setResponseApplicationVersion(applicationVersion);
    consultation.setRespondedByWuaId(responderUser.wuaId());
    consultation.setRespondedAtDatetime(clock.instant());
    consultation.setHabitatsRegsResponseType(habitatsRegsResponseType);
    consultation.setHabitatsRegsResponseDescription(habitatsRegsResponseDescription);
    consultation.setEiaRegsResponseType(eiaRegsResponseType);
    consultation.setEiaRegsResponseDescription(eiaRegsResponseDescription);
    consultation = repository.save(consultation);

    fieldConsentsFileService.saveDocuments(ConsultationFileUsage.responseUsageFrom(consultation), documents);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        consultation.getRequestApplicationVersion(),
        responderUser,
        CONSULTATION_RESPONSE,
        REGULATOR
    );

    try {
      consultationEmailService.sendConsultationResponseEmail(consultation);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a consultation response notification by user with wuaId [{}] for application \
              version with id [{}] failed. \
              Note: this hasn't prevented the consultation response being saved.
              """,
          responderUser.wuaId(), applicationVersion.getId(), exception);
    }
  }

  public boolean requiresEiaRegsResponse(ApplicationVersion applicationVersion) {
    var applicationType = applicationVersion.getApplication().getType();
    return ApplicationTypeFeature.EIA_SCREENING_DIRECTION.allowed(applicationType);
  }

}
