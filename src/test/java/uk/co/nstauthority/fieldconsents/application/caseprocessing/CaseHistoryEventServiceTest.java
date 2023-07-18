package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationCaseEventService;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseHistoryEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes.CaseNote;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes.CaseNoteEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewCaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.ApplicationWithdrawal;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalCaseEventService;

@ExtendWith(MockitoExtension.class)
class CaseHistoryEventServiceTest {

  @Mock
  private CaseNoteEventService caseNoteEventService;

  @Mock
  private WithdrawalCaseEventService withdrawalCaseEventService;

  @Mock
  private ApplicationCaseEventService applicationCaseEventService;

  @Mock
  private TechnicalReviewCaseEventService technicalReviewCaseEventService;

  private CaseHistoryEventService caseHistoryEventService;

  private ApplicationVersion applicationVersion;

  private CaseEvent applicationCreatedEvent;

  private CaseEvent applicationSubmittedEvent;

  private CaseEvent applicationWithdrawalRequestedEvent;

  private CaseEvent applicationWithdrawalRespondedEvent;

  private CaseEvent technicalReviewRequestedEvent;

  private CaseEvent technicalReviewRespondedEvent;

  @BeforeEach
  void setUp() {
    var caseEventServices = List.of(
        caseNoteEventService,
        withdrawalCaseEventService,
        applicationCaseEventService,
        technicalReviewCaseEventService
    );

    caseHistoryEventService = new CaseHistoryEventService(caseEventServices);

    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    applicationCreatedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationCreated(applicationVersion);
    applicationSubmittedEvent = CaseHistoryEventTestUtil.getCaseEventForApplicationSubmitted(applicationVersion);

    when(applicationCaseEventService.getCaseEvents(applicationVersion.getApplication())).thenReturn(
        List.of(
            applicationCreatedEvent,
            applicationSubmittedEvent
        )
    );
  }


  @Test
  void getCaseHistoryEvents_applicationJustSubmitted() {
    var caseHistoryEvents = caseHistoryEventService.getCaseHistoryEvents(applicationVersion.getApplication());

    assertThat(caseHistoryEvents).containsExactly(
        applicationSubmittedEvent,
        applicationCreatedEvent
    );
  }

  @Test
  void getCaseHistoryEvents_withCaseNote() {
    CaseNote caseNote = CaseHistoryEventTestUtil.getCaseNote(applicationVersion);
    CaseEvent caseNoteAddedEvent = CaseHistoryEventTestUtil.getCaseEventForCaseNoteAdded(caseNote);

    when(caseNoteEventService.getCaseEvents(applicationVersion.getApplication())).thenReturn(
        Collections.singletonList(caseNoteAddedEvent)
    );

    var caseHistoryEvents = caseHistoryEventService.getCaseHistoryEvents(applicationVersion.getApplication());

    assertThat(caseHistoryEvents).containsExactly(
        caseNoteAddedEvent,
        applicationSubmittedEvent,
        applicationCreatedEvent
    );
  }

  @Test
  void getCaseHistoryEvents_withWithdrawalRequest() {
    ApplicationWithdrawal applicationWithdrawal = CaseHistoryEventTestUtil.getApplicationWithdrawalRequest(applicationVersion);

    applicationWithdrawalRequestedEvent = CaseHistoryEventTestUtil.getCaseEventForWithdrawalRequested(applicationWithdrawal);
    when(withdrawalCaseEventService.getCaseEvents(applicationVersion.getApplication())).thenReturn(Collections.singletonList(applicationWithdrawalRequestedEvent));

    var caseHistoryEvents = caseHistoryEventService.getCaseHistoryEvents(applicationVersion.getApplication());

    assertThat(caseHistoryEvents).containsExactly(
        applicationWithdrawalRequestedEvent,
        applicationSubmittedEvent,
        applicationCreatedEvent
    );
  }

  @Test
  void getCaseHistoryEvents_withWithdrawalResponse() {
    ApplicationWithdrawal applicationWithdrawal = CaseHistoryEventTestUtil.getApplicationWithdrawalWithResponse(applicationVersion);

    applicationWithdrawalRequestedEvent = CaseHistoryEventTestUtil.getCaseEventForWithdrawalRequested(applicationWithdrawal);
    applicationWithdrawalRespondedEvent = CaseHistoryEventTestUtil.getCaseEventForWithdrawalResponded(applicationWithdrawal);

    when(withdrawalCaseEventService.getCaseEvents(applicationVersion.getApplication())).thenReturn(
        List.of(
            applicationWithdrawalRequestedEvent,
            applicationWithdrawalRespondedEvent
        )
    );

    var caseHistoryEvents = caseHistoryEventService.getCaseHistoryEvents(applicationVersion.getApplication());

    assertThat(caseHistoryEvents).containsExactly(
        applicationWithdrawalRespondedEvent,
        applicationWithdrawalRequestedEvent,
        applicationSubmittedEvent,
        applicationCreatedEvent
    );
  }

  @Test
  void getCaseHistoryEvents_withTechnicalReviewRequest() {
    TechnicalReview technicalReview = CaseHistoryEventTestUtil.getTechnicalReviewRequest(applicationVersion);

    technicalReviewRequestedEvent = CaseHistoryEventTestUtil.getCaseEventForTechnicalReviewRequested(technicalReview);
    when(technicalReviewCaseEventService.getCaseEvents(applicationVersion.getApplication()))
        .thenReturn(Collections.singletonList(technicalReviewRequestedEvent));

    var caseHistoryEvents = caseHistoryEventService.getCaseHistoryEvents(applicationVersion.getApplication());

    assertThat(caseHistoryEvents).containsExactly(
        technicalReviewRequestedEvent,
        applicationSubmittedEvent,
        applicationCreatedEvent
    );
  }

  @Test
  void getCaseHistoryEvents_withTechnicalReviewResponse() {
    TechnicalReview technicalReview = CaseHistoryEventTestUtil.getTechnicalReviewResponse(applicationVersion, TechnicalReviewResponseType.REJECT);

    technicalReviewRequestedEvent = CaseHistoryEventTestUtil.getCaseEventForTechnicalReviewRequested(technicalReview);
    technicalReviewRespondedEvent = CaseHistoryEventTestUtil.getCaseEventForTechnicalReviewResponded(technicalReview);
    when(technicalReviewCaseEventService.getCaseEvents(applicationVersion.getApplication())).thenReturn(
        List.of(
            technicalReviewRequestedEvent,
            technicalReviewRespondedEvent
        )
    );

    var caseHistoryEvents = caseHistoryEventService.getCaseHistoryEvents(applicationVersion.getApplication());

    assertThat(caseHistoryEvents).containsExactly(
        technicalReviewRespondedEvent,
        technicalReviewRequestedEvent,
        applicationSubmittedEvent,
        applicationCreatedEvent
    );
  }
}
