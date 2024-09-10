package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents;

import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;

import java.time.Clock;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
public class BulkIssueConsentsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BulkIssueConsentsService.class);

  private final Clock clock;
  private final BulkIssueConsentTaskRepository bulkIssueConsentTaskRepository;
  private final BulkIssueConsentRunRepository bulkIssueConsentRunRepository;
  private final EnergyPortalUserService energyPortalUserService;
  private final ConsentIssuingService consentIssuingService;
  private final BulkIssueConsentEmailService bulkIssueConsentEmailService;

  BulkIssueConsentsService(
      Clock clock,
      BulkIssueConsentTaskRepository bulkIssueConsentTaskRepository,
      BulkIssueConsentRunRepository bulkIssueConsentRunRepository,
      EnergyPortalUserService energyPortalUserService,
      ConsentIssuingService consentIssuingService,
      BulkIssueConsentEmailService bulkIssueConsentEmailService
  ) {
    this.clock = clock;
    this.bulkIssueConsentTaskRepository = bulkIssueConsentTaskRepository;
    this.bulkIssueConsentRunRepository = bulkIssueConsentRunRepository;
    this.energyPortalUserService = energyPortalUserService;
    this.consentIssuingService = consentIssuingService;
    this.bulkIssueConsentEmailService = bulkIssueConsentEmailService;
  }

  public long getCountOfConsentsNotYetIssued() {
    return bulkIssueConsentTaskRepository.countAllByFinishedAtIsNull();
  }

  @Transactional
  public void queueApplicationsForIssue(
      Collection<ApplicationVersion> applicationVersions,
      ServiceUserDetail user
  ) {
    var run = new BulkIssueConsentRun(user.wuaId());
    var taskCreatedAt = clock.instant();
    var tasks = applicationVersions.stream()
        .map(applicationVersion -> {
          var task = new BulkIssueConsentsTask();
          task.setApplicationVersion(applicationVersion);
          task.setCreatedAt(taskCreatedAt);
          task.setBulkIssueConsentRun(run);
          return task;
        })
        .toList();

    bulkIssueConsentRunRepository.save(run);
    bulkIssueConsentTaskRepository.saveAll(tasks);
  }

  @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
  @SchedulerLock(name = "BulkIssueConsentsTaskService_bulkIssueConsents", lockAtMostFor = "PT1H")
  void bulkIssueConsents() {
    assertLocked();

    var nonFinishedTasks = bulkIssueConsentTaskRepository.findAllByFinishedAtIsNull();
    if (nonFinishedTasks.isEmpty()) {
      return;
    }

    LOGGER.info("Found {} non-finished bulk consent issue tasks", nonFinishedTasks.size());

    Map<BulkIssueConsentRun, ServiceUserDetail> runToUser = new HashMap<>();

    nonFinishedTasks.forEach(task -> {
      var bulkIssueConsentRun = task.getBulkIssueConsentRun();

      var user = runToUser.computeIfAbsent(
          bulkIssueConsentRun,
          run -> energyPortalUserService.getServiceUserByWuaId(WebUserAccountId.from(bulkIssueConsentRun.getIssuedByWuaId())));

      issueConsent(task, user);
    });
  }

  void issueConsent(BulkIssueConsentsTask task, ServiceUserDetail user) {
    task.setStartedAt(clock.instant());

    try {
      var consent = consentIssuingService.issueConsent(task.getApplicationVersion(), user);
      task.setConsent(consent);
    } catch (RuntimeException e) {
      task.setErrorDetails(e.getMessage());
      LOGGER.error("Error running bulk issue consent task for application version {}", task.getApplicationVersion().getId(), e);
    }
    task.setFinishedAt(clock.instant());
    bulkIssueConsentTaskRepository.save(task);
    sendEmailsIfAllIssued(task.getBulkIssueConsentRun());
  }

  void sendEmailsIfAllIssued(BulkIssueConsentRun run) {
    var consentRunFinished = bulkIssueConsentTaskRepository.countAllByFinishedAtIsNullAndBulkIssueConsentRun(run) == 0;

    if (consentRunFinished) {
      var tasks = bulkIssueConsentTaskRepository.findAllByBulkIssueConsentRun(run);

      bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToRegulators(run, tasks);
      bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToOperators(run, tasks);
      bulkIssueConsentEmailService.sendBulkConsentIssuedEmailToFieldEquityPartners(run, tasks);
    }
  }

}
