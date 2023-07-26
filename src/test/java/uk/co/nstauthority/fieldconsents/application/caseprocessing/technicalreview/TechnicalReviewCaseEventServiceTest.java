package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil.getPortalUsersDtosMap;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.TECHNICAL_REVIEW_COMPLETED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.TECHNICAL_REVIEW_REQUESTED;

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
class TechnicalReviewCaseEventServiceTest {

  @Mock
  private TechnicalReviewService technicalReviewService;

  @InjectMocks
  private TechnicalReviewCaseEventService technicalReviewCaseEventService;

  private ApplicationVersion applicationVersion;

  private TechnicalReview technicalReviewRejected;

  private TechnicalReview technicalReviewApproved;

  private CaseEvent firstTechnicalReviewRequestedEvent;

  private CaseEvent secondTechnicalReviewRequestedEvent;

  private CaseEvent firstTechnicalReviewRejectedEvent;

  private CaseEvent secondTechnicalReviewApprovedEvent;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);

    technicalReviewRejected = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion,
        TechnicalReviewResponseType.REJECT
    );

    technicalReviewApproved = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion,
        TechnicalReviewResponseType.APPROVE
    );

    firstTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewRejected);

    firstTechnicalReviewRejectedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewResponded(technicalReviewRejected);

    secondTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewApproved);

    secondTechnicalReviewApprovedEvent = CaseHistoryEventTestUtil
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

    List<CaseEvent> caseEvents = technicalReviewCaseEventService.getCaseEvents(
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

    List<CaseEvent> caseEvents = technicalReviewCaseEventService.getCaseEvents(applicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactly(
            firstTechnicalReviewRequestedEvent,
            firstTechnicalReviewRejectedEvent,
            secondTechnicalReviewRequestedEvent,
            secondTechnicalReviewApprovedEvent
        );
  }
}
