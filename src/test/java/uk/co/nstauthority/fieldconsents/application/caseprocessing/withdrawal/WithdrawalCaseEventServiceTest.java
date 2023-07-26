package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil.getPortalUsersDtosMap;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_WITHDRAWAL_REQUESTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPLICATION_WITHDRAWAL_RESPONDED;

import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventView;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class WithdrawalCaseEventServiceTest {

  @Mock
  ApplicationWithdrawalService applicationWithdrawalService;

  @InjectMocks
  private WithdrawalCaseEventService withdrawalCaseEventService;

  private ApplicationVersion applicationVersion;

  private ApplicationWithdrawal applicationWithdrawalRejected;

  private ApplicationWithdrawal applicationWithdrawalAccepted;

  private CaseEvent firstWithdrawalRequestedEvent;

  private CaseEvent secondWithdrawalRequestedEvent;

  private CaseEvent firstWithdrawalRespondedEvent;

  private CaseEvent secondWithdrawalRespondedEvent;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);

    applicationWithdrawalRejected = ApplicationWithdrawalTestUtil.getApplicationWithdrawalWithStatus(
        applicationVersion,
        WithdrawalStatus.REJECTED
    );

    applicationWithdrawalAccepted = ApplicationWithdrawalTestUtil.getApplicationWithdrawalWithStatus(
        applicationVersion,
        WithdrawalStatus.ACCEPTED
    );

    firstWithdrawalRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForWithdrawalRequested(applicationWithdrawalRejected);

    firstWithdrawalRespondedEvent = CaseHistoryEventTestUtil
        .getCaseEventForWithdrawalResponded(applicationWithdrawalRejected);

    secondWithdrawalRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForWithdrawalRequested(applicationWithdrawalAccepted);

    secondWithdrawalRespondedEvent = CaseHistoryEventTestUtil
        .getCaseEventForWithdrawalResponded(applicationWithdrawalAccepted);
  }

  @Test
  void getCaseEvents_withNoOpenRequest() {
    when(applicationWithdrawalService.getApplicationWithdrawalsByApplication(applicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());

    assertThat(
        withdrawalCaseEventService.getCaseEvents(applicationVersion.getApplication())
    ).isEmpty();
  }

  @Test
  void getCaseEvents_withOneOpenRequest() {
    var applicationVersionForProduction = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    var applicationWithdrawalOpen = ApplicationWithdrawalTestUtil.getApplicationWithdrawalWithStatus(
        applicationVersionForProduction,
        WithdrawalStatus.OPEN
    );

    firstWithdrawalRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForWithdrawalRequested(applicationWithdrawalOpen);

    when(applicationWithdrawalService.getApplicationWithdrawalsByApplication(applicationVersion.getApplication()))
        .thenReturn(
            Collections.singletonList(
                applicationWithdrawalOpen
            )
        );

    List<CaseEvent> caseEvents = withdrawalCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            firstWithdrawalRequestedEvent
        );
  }

  @Test
  void getCaseEvents_withOneRejectedAndOneAcceptedRequest() {
    when(applicationWithdrawalService.getApplicationWithdrawalsByApplication(applicationVersion.getApplication()))
        .thenReturn(
            List.of(
                applicationWithdrawalRejected,
                applicationWithdrawalAccepted
            )
        );

    List<CaseEvent> caseEvents = withdrawalCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            firstWithdrawalRequestedEvent,
            firstWithdrawalRespondedEvent,
            secondWithdrawalRequestedEvent,
            secondWithdrawalRespondedEvent
        );
  }
}
