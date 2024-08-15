package uk.co.nstauthority.fieldconsents.application.bulkcaseactions.bulkissueconsents.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.ConsentIssuingService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class BulkIssueConsentsTaskServiceTest {

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Mock
  private BulkIssueConsentTaskRepository bulkIssueConsentTaskRepository;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private ConsentIssuingService consentIssuingService;

  private BulkIssueConsentsTaskService bulkIssueConsentsTaskService;

  @BeforeEach
  void setUp() {
    this.bulkIssueConsentsTaskService = spy(new BulkIssueConsentsTaskService(
      clock,
      bulkIssueConsentTaskRepository,
      energyPortalUserService,
      consentIssuingService
    ));
  }

  @Test
  void getConsentsPendingIssue() {
    var consentsPendingIssue = 10L;
    when(bulkIssueConsentTaskRepository.countAllByFinishedAtIsNull()).thenReturn(consentsPendingIssue);
    assertThat(bulkIssueConsentsTaskService.getConsentsPendingIssue()).isEqualTo(consentsPendingIssue);
  }

  @Test
  void queueApplicationsForConsentIssue() {
    var applicationVersions = IntStream.range(0, 10).mapToObj(i -> new ApplicationVersion()).toList();
    var user = ServiceUserDetailTestUtil.Builder().build();

    bulkIssueConsentsTaskService.queueApplicationsForConsentIssue(applicationVersions, user);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<BulkIssueConsentsTask>> bulkIssueConsentTasksCaptor = ArgumentCaptor.forClass(List.class);
    verify(bulkIssueConsentTaskRepository).saveAll(bulkIssueConsentTasksCaptor.capture());

    var bulkIssueConsentTasks = bulkIssueConsentTasksCaptor.getValue();

    for (var i = 0; i < bulkIssueConsentTasks.size(); i++) {
      var bulkConsentIssueTask = bulkIssueConsentTasks.get(i);

      assertThat(bulkConsentIssueTask.getCreatedAt()).isEqualTo(clock.instant());
      assertThat(bulkConsentIssueTask.getCreatedByWuaId()).isEqualTo(user.wuaId());
      assertThat(bulkConsentIssueTask.getApplicationVersion()).isEqualTo(applicationVersions.get(i));
      assertThat(bulkConsentIssueTask.getStartedAt()).isNull();
      assertThat(bulkConsentIssueTask.getFinishedAt()).isNull();
      assertThat(bulkConsentIssueTask.getErrorDetails()).isNull();
    }
  }

  @Test
  void bulkIssueConsents() {
    LockAssert.TestHelper.makeAllAssertsPass(true);

    var nonFinishedTasks = IntStream.range(0, 10).mapToObj(i -> new BulkIssueConsentsTask()).toList();

    when(bulkIssueConsentTaskRepository.findAllByFinishedAtIsNull()).thenReturn(nonFinishedTasks);
    doNothing().when(bulkIssueConsentsTaskService).issueConsent(any()); // this tested below

    bulkIssueConsentsTaskService.bulkIssueConsents();

    for (var nonFinishedTask : nonFinishedTasks) {
      verify(bulkIssueConsentsTaskService).issueConsent(nonFinishedTask);
    }
  }

  @Test
  void issueConsent() {
    var bulkConsentIssueTask = new BulkIssueConsentsTask();
    var applicationVersion = new ApplicationVersion();
    var userWuaId = 123L;
    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    var user = ServiceUserDetail.from(energyPortalUser);

    bulkConsentIssueTask.setApplicationVersion(applicationVersion);
    bulkConsentIssueTask.setCreatedByWuaId(userWuaId);

    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(userWuaId))).thenReturn(energyPortalUser);

    bulkIssueConsentsTaskService.issueConsent(bulkConsentIssueTask);

    verify(consentIssuingService).issueConsent(applicationVersion, user);

    var bulkIssueConsentTaskCaptor = ArgumentCaptor.forClass(BulkIssueConsentsTask.class);
    verify(bulkIssueConsentTaskRepository).save(bulkIssueConsentTaskCaptor.capture());
    assertThat(bulkConsentIssueTask)
        .extracting(
            BulkIssueConsentsTask::getStartedAt,
            BulkIssueConsentsTask::getFinishedAt,
            BulkIssueConsentsTask::getErrorDetails
        )
        .containsExactly(
            clock.instant(),
            clock.instant(),
            null
        );
  }

  @Test
  void issueConsent_exceptionWhenIssuingConsent() {
    var bulkConsentIssueTask = new BulkIssueConsentsTask();
    var applicationVersion = new ApplicationVersion();
    var userWuaId = 123L;
    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    var user = ServiceUserDetail.from(energyPortalUser);

    bulkConsentIssueTask.setApplicationVersion(applicationVersion);
    bulkConsentIssueTask.setCreatedByWuaId(userWuaId);

    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(userWuaId))).thenReturn(energyPortalUser);
    doThrow(new RuntimeException("Error signing PDF document")).when(consentIssuingService).issueConsent(applicationVersion, user);

    bulkIssueConsentsTaskService.issueConsent(bulkConsentIssueTask);

    verify(consentIssuingService).issueConsent(applicationVersion, user);

    var bulkIssueConsentTaskCaptor = ArgumentCaptor.forClass(BulkIssueConsentsTask.class);
    verify(bulkIssueConsentTaskRepository).save(bulkIssueConsentTaskCaptor.capture());
    assertThat(bulkConsentIssueTask)
        .extracting(
            BulkIssueConsentsTask::getStartedAt,
            BulkIssueConsentsTask::getFinishedAt,
            BulkIssueConsentsTask::getErrorDetails
        )
        .containsExactly(
            clock.instant(),
            clock.instant(),
            "Error signing PDF document"
        );
  }
}