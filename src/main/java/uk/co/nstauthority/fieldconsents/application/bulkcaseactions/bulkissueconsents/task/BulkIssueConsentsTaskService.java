package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.task;

import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;

import java.time.Clock;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentIssuingService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@Service
public class BulkIssueConsentsTaskService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BulkIssueConsentsTaskService.class);

  private final Clock clock;
  private final BulkIssueConsentTaskRepository bulkIssueConsentTaskRepository;
  private final EnergyPortalUserService energyPortalUserService;
  private final ConsentIssuingService consentIssuingService;

  BulkIssueConsentsTaskService(
      Clock clock,
      BulkIssueConsentTaskRepository bulkIssueConsentTaskRepository,
      EnergyPortalUserService energyPortalUserService,
      ConsentIssuingService consentIssuingService
  ) {
    this.clock = clock;
    this.bulkIssueConsentTaskRepository = bulkIssueConsentTaskRepository;
    this.energyPortalUserService = energyPortalUserService;
    this.consentIssuingService = consentIssuingService;
  }

  public long getConsentsPendingIssue() {
    return bulkIssueConsentTaskRepository.countAllByFinishedAtIsNull();
  }

  @Transactional
  public void queueApplicationsForConsentIssue(
      Collection<ApplicationVersion> applicationVersions,
      ServiceUserDetail user
  ) {
    var now = clock.instant();
    var wuaId = user.wuaId();
    var tasks = applicationVersions.stream()
        .map(applicationVersion -> {
          var task = new BulkIssueConsentsTask();
          task.setApplicationVersion(applicationVersion);
          task.setCreatedAt(now);
          task.setCreatedByWuaId(wuaId);
          return task;
        })
        .toList();

    bulkIssueConsentTaskRepository.saveAll(tasks);
  }

  @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
  @SchedulerLock(name = "BulkIssueConsentsTaskService_bulkIssueConsents", lockAtMostFor = "PT1H")
  void bulkIssueConsents() {
    assertLocked();

    var nonFinishedTasks = bulkIssueConsentTaskRepository.findAllByFinishedAtIsNull();
    if (nonFinishedTasks.isEmpty()) {
      return;
    }

    LOGGER.info("Found {} non-finished bulk consent issue tasks", nonFinishedTasks.size());
    nonFinishedTasks.forEach(this::issueConsent);
  }

  void issueConsent(BulkIssueConsentsTask task) {
    task.setStartedAt(clock.instant());

    var applicationVersion = task.getApplicationVersion();
    try {
      var energyPortalUser = energyPortalUserService.getByWuaId(WebUserAccountId.from(task.getCreatedByWuaId()));
      var serviceUserDetail = ServiceUserDetail.from(energyPortalUser);
      consentIssuingService.issueConsent(applicationVersion, serviceUserDetail);
    } catch (RuntimeException e) {
      task.setErrorDetails(e.getMessage());
      LOGGER.error("Error running bulk issue consent task for application version {}", applicationVersion.getId(), e);
    }

    task.setFinishedAt(clock.instant());

    bulkIssueConsentTaskRepository.save(task);
  }

}
