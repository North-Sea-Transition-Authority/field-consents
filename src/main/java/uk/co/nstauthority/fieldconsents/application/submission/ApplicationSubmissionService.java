package uk.co.nstauthority.fieldconsents.application.submission;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.APPLICATION_SUBMITTED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.UPDATE_SUBMITTED;

import io.micrometer.observation.annotation.Observed;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.aceflag.AceFlagService;
import uk.co.nstauthority.fieldconsents.application.tasklist.shared.ApplicationTaskListService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@Service
public class ApplicationSubmissionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSubmissionService.class);

  private final Clock clock;
  private final AceFlagService aceFlagService;
  private final ApplicationService applicationService;
  private final ApplicationRepository applicationRepository;
  private final ApplicationTaskListService applicationTaskListService;
  private final ApplicationVersionRepository applicationVersionRepository;
  private final ApplicationSubmissionEmailService applicationSubmissionEmailService;
  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  ApplicationSubmissionService(
      Clock clock,
      AceFlagService aceFlagService,
      ApplicationService applicationService,
      ApplicationRepository applicationRepository,
      ApplicationTaskListService applicationTaskListService,
      ApplicationVersionRepository applicationVersionRepository,
      ApplicationSubmissionEmailService applicationSubmissionEmailService,
      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService
  ) {
    this.clock = clock;
    this.aceFlagService = aceFlagService;
    this.applicationService = applicationService;
    this.applicationRepository = applicationRepository;
    this.applicationTaskListService = applicationTaskListService;
    this.applicationVersionRepository = applicationVersionRepository;
    this.applicationSubmissionEmailService = applicationSubmissionEmailService;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
  }

  public boolean isSubmittable(ApplicationVersion applicationVersion) {
    return applicationTaskListService.getAllSections(applicationVersion)
        .stream()
        .allMatch(TaskListSection::isCompleted);
  }

  @Transactional
  @Observed(name = "fcs.application.submitted", contextualName = "application submitted")
  public void submitApplication(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationVersionStatus = applicationVersion.getStatus();
    if (!ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersionStatus)
        && !ApplicationVersionStatus.AWAITING_PAYMENT.equals(applicationVersionStatus)) {
      throw new IllegalStateException(
          String.format(
              "Application %d cannot be submitted as application version has status %s",
              applicationVersion.getApplication().getId(),
              applicationVersionStatus
          )
      );
    }

    var application = applicationVersion.getApplication();

    // If the application has not been in AWAITING_PAYMENT status if a payment is not required, it won't have a number,
    // so assign one.
    if (application.getApplicationNo() == null) {
      application.setApplicationNo(applicationService.getNextApplicationNumber());
      applicationRepository.save(application);
    }

    submitApplicationVersion(applicationVersion, user);

    if (!aceFlagService.isAceApplication(applicationVersion)) {
      try {
        applicationSubmissionEmailService.sendNonAceApplicationSubmissionEmail(applicationVersion);
      } catch (Exception exception) {
        LOGGER.error("""
              An attempt to send a non-ACE application submission notification to the regulator \
              by user with wuaId [{}] for application version with id [{}] failed. \
              Note: this hasn't prevented the non-ACE application being submitted.
              """,
            user.wuaId(), applicationVersion.getId(), exception);
      }
    }
  }

  @Transactional
  public void regulatorAutoSubmitApplication(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationVersionStatus = applicationVersion.getStatus();
    if (!ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersionStatus)) {
      throw new IllegalStateException(
          String.format(
              "Application %d cannot be submitted as application version has status %s",
              applicationVersion.getApplication().getId(),
              applicationVersionStatus
          )
      );
    }

    submitApplicationVersion(applicationVersion, user);
  }

  private void submitApplicationVersion(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    setApplicationVersionAsSubmitted(applicationVersion, user);
    aceFlagService.autoSetAceFlag(applicationVersion);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_SUBMITTED, INDUSTRY);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, APPLICATION_SUBMITTED, REGULATOR);
  }

  @Transactional
  @Observed(name = "fcs.application.update-submitted", contextualName = "application update submitted")
  public void submitApplicationUpdate(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    var applicationVersionStatus = applicationVersion.getStatus();
    if (!ApplicationVersionStatus.IN_PROGRESS.equals(applicationVersionStatus)) {
      throw new IllegalStateException(
          String.format(
              "Application update cannot be submitted for application %d as application version has status %s",
              applicationVersion.getApplication().getId(),
              applicationVersionStatus
          )
      );
    }

    setApplicationVersionAsSubmitted(applicationVersion, user);

    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, UPDATE_SUBMITTED, INDUSTRY);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, UPDATE_SUBMITTED, REGULATOR);
    applicationWorkAreaPriorityService
        .prioritiseApplicationInWorkArea(applicationVersion, user, UPDATE_SUBMITTED, REGULATOR_TECHNICAL_REVIEWER);
  }

  private void setApplicationVersionAsSubmitted(ApplicationVersion applicationVersion, ServiceUserDetail user) {
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);
    applicationVersion.setSubmittedDateTime(clock.instant());
    applicationVersion.setSubmittedByWuaId(user.wuaId());
    applicationVersionRepository.save(applicationVersion);
  }
}
