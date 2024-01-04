package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
class TechnicalReviewCaseEventServiceTest {

  @Mock
  private TechnicalReviewService technicalReviewService;

  @InjectMocks
  private TechnicalReviewCaseEventService technicalReviewCaseEventService;

  private ApplicationVersion applicationVersion;

  private ApplicationVersion newerApplicationVersion;

  private TechnicalReview technicalReviewRejected;

  private TechnicalReview technicalReviewApproved;

  private CaseEvent firstTechnicalReviewRequestedEvent;

  private CaseEvent secondTechnicalReviewRequestedEvent;

  private CaseEvent firstTechnicalReviewResponseEvent;

  private CaseEvent secondTechnicalReviewResponseEvent;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    newerApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    newerApplicationVersion.setId(applicationVersion.getId() + 1);

    technicalReviewRejected = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion,
        TechnicalReviewResponseType.REJECT
    );
    technicalReviewRejected.setResponseApplicationVersion(newerApplicationVersion);

    technicalReviewApproved = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion,
        TechnicalReviewResponseType.APPROVE
    );
    technicalReviewApproved.setResponseApplicationVersion(newerApplicationVersion);

    firstTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewRejected);

    firstTechnicalReviewResponseEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewResponded(technicalReviewRejected);

    secondTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewApproved);

    secondTechnicalReviewResponseEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewResponded(technicalReviewApproved);
  }

  @Test
  void getCaseEvents_withNoOpenRequest() {
    when(technicalReviewService.getTechnicalReviewsByApplication(applicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());

    assertThat(
        technicalReviewCaseEventService.getCaseEvents(applicationVersion.getApplication())
    ).isEmpty();
  }

  @Test
  void getCaseEvents_withOneOpenRequest() {
    var applicationVersionForProduction = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    var technicalReviewOpen = TechnicalReviewTestUtil.getOpenTechnicalReview(applicationVersionForProduction);

    firstTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewOpen);

    when(technicalReviewService.getTechnicalReviewsByApplication(applicationVersionForProduction.getApplication()))
        .thenReturn(
            Collections.singletonList(
                technicalReviewOpen
            )
        );

    var caseEvents = technicalReviewCaseEventService.getCaseEvents(
        applicationVersionForProduction.getApplication()
    );

    assertThat(caseEvents)
        .containsExactly(
            firstTechnicalReviewRequestedEvent
        );
  }

  @Test
  void getCaseEvents() {
    when(technicalReviewService.getTechnicalReviewsByApplication(applicationVersion.getApplication()))
        .thenReturn(
            List.of(
                technicalReviewRejected,
                technicalReviewApproved
            )
        );

    var caseEvents = technicalReviewCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            firstTechnicalReviewRequestedEvent,
            firstTechnicalReviewResponseEvent,
            secondTechnicalReviewRequestedEvent,
            secondTechnicalReviewResponseEvent
        );
  }

  @Test
  void getCaseEvents_withMissingRequestAndResponseData() {
    var technicalReview1 = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion,
        null
    );
    technicalReview1.setDeadlineDateTime(null);
    technicalReview1.setRequestText(null);
    technicalReview1.setResponseApplicationVersion(applicationVersion);
    technicalReview1.setResponseText(null);

    var technicalReview2 = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion,
        null
    );
    technicalReview2.setDeadlineDateTime(null);
    technicalReview2.setRequestText(null);
    technicalReview2.setResponseApplicationVersion(newerApplicationVersion);
    technicalReview2.setResponseText(null);

    firstTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReview1);

    firstTechnicalReviewResponseEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewResponded(technicalReview1);

    secondTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReview2);

    secondTechnicalReviewResponseEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewResponded(technicalReview2);

    when(technicalReviewService.getTechnicalReviewsByApplication(applicationVersion.getApplication()))
        .thenReturn(
            List.of(
                technicalReview1,
                technicalReview2
            )
        );

    var caseEvents = technicalReviewCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            firstTechnicalReviewRequestedEvent,
            firstTechnicalReviewResponseEvent,
            secondTechnicalReviewRequestedEvent,
            secondTechnicalReviewResponseEvent
        );
  }
}
