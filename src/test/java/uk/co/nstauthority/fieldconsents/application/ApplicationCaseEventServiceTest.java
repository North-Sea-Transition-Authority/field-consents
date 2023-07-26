package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil.getPortalUsersDtosMap;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_CREATED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_SUBMITTED;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class ApplicationCaseEventServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @InjectMocks
  private ApplicationCaseEventService applicationCaseEventService;

  private ApplicationVersion applicationVersion;

  private ApplicationVersion applicationVersionUpdate;

  private CaseEvent applicationCreatedEvent;

  private CaseEvent applicationSubmittedEvent;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    applicationVersionUpdate = ApplicationTestUtil.getSubmittedApplicationVersionWithTypeIdAndVersionNumber(
        ApplicationType.FLARE, 2, 2
    );

    applicationCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(applicationVersion);
    applicationSubmittedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationSubmitted(applicationVersion);
  }

  @Test
  void getCaseEvents_whenFirstApplicationSubmitted() {
    when(applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(Collections.singletonList(applicationVersion));

    List<CaseEvent> caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationSubmittedEvent
        );
  }

  @Test
  void getCaseEvents_withApplicationUpdateSubmitted() {
    var applicationUpdateCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(
        applicationVersionUpdate);
    var applicationUpdateSubmittedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationSubmitted(
        applicationVersionUpdate);

    when(
        applicationVersionService.getAllApplicationVersionsByApplicationId(applicationVersion.getApplication().getId()))
        .thenReturn(List.of(
                applicationVersion,
                applicationVersionUpdate
            )
        );

    List<CaseEvent> caseEvents = applicationCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            applicationCreatedEvent,
            applicationSubmittedEvent,
            applicationUpdateCreatedEvent,
            applicationUpdateSubmittedEvent
        );
  }
}
