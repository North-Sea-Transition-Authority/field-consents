package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.APPLICATION_UPDATE_RESPONSE_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType.OTHER_CHANGES;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response.ApplicationUpdateResponseType.REQUESTED_CHANGES_ONLY;

import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateCaseEventServiceTest {

  @Mock
  private Clock clock;

  @Mock
  private ApplicationUpdateService applicationUpdateService;

  @InjectMocks
  private ApplicationUpdateCaseEventService applicationUpdateCaseEventService;

  private ApplicationVersion applicationVersion;

  private ApplicationVersion applicationVersionUpdate;

  private CaseEvent updateRequestedEvent;

  private ApplicationUpdate openApplicationUpdate;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    applicationVersionUpdate = ApplicationTestUtil.getSubmittedApplicationVersionWithTypeIdAndVersionNumber(
        ApplicationType.FLARE, 2, 2
    );
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    openApplicationUpdate = ApplicationUpdateTestUtil.getOpenApplicationUpdate(applicationVersion, clock);
  }

  @Test
  void getCaseEvents_withApplicationUpdateRequested() {
    updateRequestedEvent = CaseHistoryEventTestUtil.getApplicationUpdateRequestedEvent(openApplicationUpdate);

    when(
        applicationUpdateService.getApplicationUpdatesByApplication(applicationVersion.getApplication()))
        .thenReturn(
            List.of(openApplicationUpdate)
        );

    var caseEvents = applicationUpdateCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            updateRequestedEvent
        );
  }

  @Test
  void getCaseEvents_withApplicationUpdateSubmitted_requestedChangesOnlyWithoutText() {
    var completedApplicationUpdate = ApplicationUpdateTestUtil.getClosedApplicationUpdate(
        applicationVersion,
        applicationVersionUpdate,
        REQUESTED_CHANGES_ONLY,
        null,
        clock
    );

    assertUpdateCaseEvents(completedApplicationUpdate);
  }

  @Test
  void getCaseEvents_withApplicationUpdateSubmitted_requestedChangesOnlyWithText() {
    var completedApplicationUpdate = ApplicationUpdateTestUtil.getClosedApplicationUpdate(
        applicationVersion,
        applicationVersionUpdate,
        REQUESTED_CHANGES_ONLY,
        APPLICATION_UPDATE_RESPONSE_TEXT,
        clock
    );

    assertUpdateCaseEvents(completedApplicationUpdate);
  }

  @Test
  void getCaseEvents_withApplicationUpdateSubmitted_otherChangesWithoutText() {
    var completedApplicationUpdate = ApplicationUpdateTestUtil.getClosedApplicationUpdate(
        applicationVersion,
        applicationVersionUpdate,
        OTHER_CHANGES,
        null,
        clock
    );

    assertUpdateCaseEvents(completedApplicationUpdate);
  }

  @Test
  void getCaseEvents_withApplicationUpdateSubmitted_otherChangesWithText() {
    var completedApplicationUpdate = ApplicationUpdateTestUtil.getClosedApplicationUpdate(
        applicationVersion,
        applicationVersionUpdate,
        OTHER_CHANGES,
        APPLICATION_UPDATE_RESPONSE_TEXT,
        clock
    );

    assertUpdateCaseEvents(completedApplicationUpdate);
  }

  @Test
  void getCaseEvents_withApplicationUpdateSubmitted_withMissingRequestAndResponseData() {
    openApplicationUpdate.setRequestText(null);
    openApplicationUpdate.setDeadlineDateTime(null);
    var completedApplicationUpdate = ApplicationUpdateTestUtil.getClosedApplicationUpdate(
        applicationVersion,
        applicationVersionUpdate,
        null,
        null,
        clock
    );
    completedApplicationUpdate.setRequestText(null);
    completedApplicationUpdate.setDeadlineDateTime(null);

    assertUpdateCaseEvents(completedApplicationUpdate);
  }

  private void assertUpdateCaseEvents(ApplicationUpdate completedApplicationUpdate) {
    updateRequestedEvent = CaseHistoryEventTestUtil.getApplicationUpdateRequestedEvent(openApplicationUpdate);
    CaseEvent updateCompletedEvent = CaseHistoryEventTestUtil.getApplicationUpdateSubmittedEvent(
        completedApplicationUpdate
    );

    when(
        applicationUpdateService.getApplicationUpdatesByApplication(applicationVersion.getApplication()))
        .thenReturn(
            List.of(completedApplicationUpdate)
        );

    var caseEvents = applicationUpdateCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            updateRequestedEvent,
            updateCompletedEvent
        );
  }

}
