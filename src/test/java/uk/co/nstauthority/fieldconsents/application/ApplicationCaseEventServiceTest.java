package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment.ApplicationVersionAuditTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;

@ExtendWith(MockitoExtension.class)
class ApplicationCaseEventServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationVersionAuditService applicationVersionAuditService;

  @InjectMocks
  private ApplicationCaseEventService applicationCaseEventService;

  private ApplicationVersion applicationVersion;

  private ApplicationVersion applicationVersionUpdate;

  private CaseEvent applicationCreatedEvent;

  private CaseEvent applicationSubmittedEvent;

  private CaseEvent applicationUpdateStartedEvent;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    applicationVersionUpdate = ApplicationTestUtil.getSubmittedApplicationVersionWithTypeIdAndVersionNumber(
        ApplicationType.FLARE, 2, 2
    );

    applicationCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(applicationVersion);
    applicationSubmittedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationSubmitted(applicationVersion);
    applicationUpdateStartedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationUpdateStarted(
        applicationVersionUpdate);

    when(applicationVersionAuditService.getApplicationVersionAudits(anyList())).thenReturn(Collections.emptyList());
  }

  @Test
  void getCaseEvents_whenFirstApplicationSubmitted() {
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(Collections.singletonList(applicationVersion));

    var  caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationSubmittedEvent
        );
  }

  @Test
  void getCaseEvents_withApplicationUpdateStarted() {
    when(
        applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(List.of(
                applicationVersion,
                applicationVersionUpdate
            )
        );

    var  caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationSubmittedEvent,
            applicationUpdateStartedEvent
        );
  }

  @Test
  void getCaseEvents_whenFirstApplicationDeleted() {
    var deletedApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    deletedApplicationVersion.setStatus(ApplicationVersionStatus.DELETED);
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(deletedApplicationVersion.getApplication().getId()))
        .thenReturn(Collections.singletonList(deletedApplicationVersion));

    var applicationDeletedAudit = ApplicationVersionAuditTestUtil.getApplicationVersionAuditApplicationDeleted(deletedApplicationVersion);
    when(applicationVersionAuditService.getApplicationVersionAudits(anyList())).thenReturn(List.of(applicationDeletedAudit));

    var applicationCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(deletedApplicationVersion);
    var applicationDeletedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationDeleted(deletedApplicationVersion, applicationDeletedAudit.auditDateTime());

    var  caseEvents = applicationCaseEventService.getCaseEvents(deletedApplicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationDeletedEvent
        );
  }

  @Test
  void getCaseEvents_withApplicationUpdateDeleted() {
    applicationVersionUpdate.setStatus(ApplicationVersionStatus.DELETED);
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersionUpdate.getApplication().getId()))
        .thenReturn(Collections.singletonList(applicationVersionUpdate));
    when(
        applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(List.of(
                applicationVersion,
                applicationVersionUpdate
            )
        );

    var applicationDeletedAudit = ApplicationVersionAuditTestUtil.getApplicationVersionAuditApplicationDeleted(applicationVersionUpdate);
    when(applicationVersionAuditService.getApplicationVersionAudits(anyList())).thenReturn(List.of(applicationDeletedAudit));
    var applicationDeletedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationUpdateDeleted(applicationVersionUpdate, applicationDeletedAudit.auditDateTime());

    var  caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationSubmittedEvent,
            applicationUpdateStartedEvent,
            applicationDeletedEvent
        );
  }
}
