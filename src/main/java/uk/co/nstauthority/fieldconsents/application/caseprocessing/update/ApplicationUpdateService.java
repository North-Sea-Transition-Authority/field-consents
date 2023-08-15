package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_UPDATE_REQUEST;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType;
import uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class ApplicationUpdateService {

  static final UnaryOperator<String> NO_OPEN_APPLICATION_UPDATE_EXISTS =
      "Open application update not found for application with version id %s"::formatted;

  static final UnaryOperator<String> OPEN_APPLICATION_UPDATE_EXISTS =
      "A application update is already open for the application with version id %s"::formatted;

  private final ApplicationService applicationService;

  private final ApplicationDuplicationService applicationDuplicationService;

  private final ApplicationUpdateRepository applicationUpdateRepository;

  private final Clock clock;

  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  @Autowired
  ApplicationUpdateService(ApplicationService applicationService,
                           ApplicationDuplicationService applicationDuplicationService,
                           ApplicationUpdateRepository applicationUpdateRepository,
                           Clock clock,
                           ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService) {
    this.applicationService = applicationService;
    this.applicationDuplicationService = applicationDuplicationService;
    this.applicationUpdateRepository = applicationUpdateRepository;
    this.clock = clock;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
  }

  public boolean openApplicationUpdateExists(ApplicationVersion applicationVersion) {
    return applicationUpdateRepository
        .existsByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN);
  }

  public Optional<ApplicationUpdate> findOpenApplicationUpdate(ApplicationVersion applicationVersion) {
    return applicationUpdateRepository
        .findByApplicationVersion_ApplicationAndApplicationUpdateStatus(applicationVersion.getApplication(), OPEN);
  }

  public ApplicationUpdate getOpenApplicationUpdate(ApplicationVersion applicationVersion) {
    return findOpenApplicationUpdate(applicationVersion)
        .orElseThrow(() -> new EntityNotFoundException(
            NO_OPEN_APPLICATION_UPDATE_EXISTS.apply(String.valueOf(applicationVersion.getApplication().getId()))));
  }

  public ApplicationUpdateRequestForm getApplicationUpdateRequestForm(ApplicationVersion applicationVersion) {
    if (openApplicationUpdateExists(applicationVersion)) {
      throw new IllegalStateException(OPEN_APPLICATION_UPDATE_EXISTS
          .apply(String.valueOf(applicationVersion.getApplication().getId())));
    }
    return new ApplicationUpdateRequestForm();
  }

  @Transactional
  public void saveApplicationUpdateRequest(ApplicationVersion applicationVersion,
                                           Instant deadlineInstant,
                                           String requestText,
                                           ServiceUserDetail user) {
    var applicationUpdate = new ApplicationUpdate();
    applicationUpdate.setApplicationVersion(applicationVersion);
    applicationUpdate.setApplicationUpdateStatus(OPEN);
    applicationUpdate.setRequestedByWuaId(user.wuaId());
    applicationUpdate.setRequestedDateTime(clock.instant());
    applicationUpdate.setRequestText(requestText);
    applicationUpdate.setDeadlineDateTime(deadlineInstant);
    applicationUpdateRepository.save(applicationUpdate);
    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        user,
        APPLICATION_UPDATE_REQUEST,
        INDUSTRY
    );
  }

  @Transactional
  public void startApplicationUpdate(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var newApplicationVersion = applicationService.startApplicationUpdate(applicationVersion, user);
    applicationDuplicationService.duplicateApplicationSections(applicationVersion, newApplicationVersion);
  }

  public List<ApplicationUpdate> getApplicationUpdatesByApplication(Application application) {
    return applicationUpdateRepository.findByApplicationVersion_Application(application);
  }

  @Transactional
  public void saveApplicationUpdateResponseAndSubmitApplicationUpdate(
      ApplicationVersion applicationVersion,
      ApplicationUpdateResponseType responseType,
      String otherChangesDescription,
      ServiceUserDetail user
  ) {
    if (!ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersion.getStatus())) {
      throw new IllegalStateException(
          "Application update for application version id %s with status %s cannot be submitted (status %s expected)"
              .formatted(
                  applicationVersion.getId(),
                  applicationVersion.getStatus().name(),
                  ApplicationVersionStatus.IN_PROGRESS.name()
              )
      );
    }

    var applicationUpdate = getOpenApplicationUpdate(applicationVersion);
    applicationUpdate.setRespondedByWuaId(user.wuaId());
    applicationUpdate.setRespondedDateTime(clock.instant());
    applicationUpdate.setResponseType(responseType);
    if (ApplicationUpdateResponseType.OTHER_CHANGES.equals(responseType)) {
      applicationUpdate.setResponseText(otherChangesDescription);
    }
    applicationUpdate.setApplicationUpdateStatus(ApplicationUpdateStatus.CLOSED);
    applicationUpdate.setResponseApplicationVersion(applicationVersion);

    applicationUpdateRepository.save(applicationUpdate);

    applicationService.submitApplicationUpdate(applicationVersion, user);
  }
}
