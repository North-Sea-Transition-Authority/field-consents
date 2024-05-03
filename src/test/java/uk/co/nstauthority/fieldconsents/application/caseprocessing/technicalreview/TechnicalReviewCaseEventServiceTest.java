package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.envers.RevisionType.ADD;
import static org.hibernate.envers.RevisionType.MOD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.TECHNICAL_REVIEW_REASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.DEADLINE_AHEAD_HOURS;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_USER_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_USER_2;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEW_ID_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEW_ID_2;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.CaseHistoryEventTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAudit;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewCaseEventServiceTest {

  private static final long AUDIT_REVISION_ID = 1L;
  private static final long AUDIT_CREATED_BY_WUA_ID = 1L;
  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private static final Instant REVISION_TIMESTAMP = CLOCK.instant().truncatedTo(ChronoUnit.MILLIS);
  private static final Instant REQUEST_DEADLINE = CLOCK.instant().plus(DEADLINE_AHEAD_HOURS, ChronoUnit.HOURS);

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private FieldConsentsAuditService fieldConsentsAuditService;

  @Captor
  private ArgumentCaptor<Collection<TechnicalReview>> technicalReviewCollectionCaptor;

  @Captor
  private ArgumentCaptor<Function<TechnicalReview, Object>> technicalReviewIdFunctionCaptor;

  @Spy
  @InjectMocks
  private TechnicalReviewCaseEventService technicalReviewCaseEventService;

  private ApplicationVersion requestApplicationVersion, responseApplicationVersion;

  private AuditRevision auditRevision;

  private TechnicalReview technicalReviewRejected;

  private TechnicalReview technicalReviewApproved;

  private CaseEvent firstTechnicalReviewRequestedEvent;

  private CaseEvent secondTechnicalReviewRequestedEvent;

  private CaseEvent firstTechnicalReviewCompletedEvent;

  private CaseEvent secondTechnicalReviewCompletedEvent;

  @BeforeEach
  void setUp() {
    requestApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);
    responseApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.VENT);

    auditRevision = new AuditRevision();
    auditRevision.setId(AUDIT_REVISION_ID);
    auditRevision.setCreatedDateTime(Date.from(REVISION_TIMESTAMP));
    auditRevision.setUserWuaId(AUDIT_CREATED_BY_WUA_ID);

    technicalReviewRejected = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        requestApplicationVersion, TechnicalReviewResponseType.REJECT);

    technicalReviewRejected.setResponseApplicationVersion(responseApplicationVersion);

    technicalReviewApproved = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        requestApplicationVersion, TechnicalReviewResponseType.APPROVE);

    technicalReviewApproved.setResponseApplicationVersion(responseApplicationVersion);
    technicalReviewApproved.setId(TECHNICAL_REVIEW_ID_2);
    technicalReviewApproved.setDeadlineDateTime(REQUEST_DEADLINE.plusSeconds(60000));

    firstTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewRejected);

    firstTechnicalReviewCompletedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewResponded(technicalReviewRejected);

    secondTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewApproved);

    secondTechnicalReviewCompletedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewResponded(technicalReviewApproved);
  }

  @Test
  void getCaseEvents_withNoOpenRequest() {
    when(technicalReviewService.getTechnicalReviewsByApplication(requestApplicationVersion.getApplication()))
        .thenReturn(Collections.emptyList());

    when(fieldConsentsAuditService.getAuditsFor(
        eq(TechnicalReview.class),
        any(),
        any()
    )).thenReturn(Collections.emptyList());

    assertThat(technicalReviewCaseEventService.getCaseEvents(requestApplicationVersion.getApplication())).isEmpty();
  }

  @Test
  void getCaseEvents_withOneOpenRequestAndMigratedCases_thenShowAuditEventFromActualData() {
    var technicalReviewOpen = TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion);

    firstTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewOpen);

    when(technicalReviewService.getTechnicalReviewsByApplication(requestApplicationVersion.getApplication()))
        .thenReturn(Collections.singletonList(technicalReviewOpen));

    var caseEvents = technicalReviewCaseEventService.getCaseEvents(requestApplicationVersion.getApplication());

    assertThat(caseEvents).containsExactly(firstTechnicalReviewRequestedEvent);
  }

  @Test
  void getCaseEvents_withOneOpenRequestAndNoMigratedCases_thenShowAuditEventFromAuditData() {
    var technicalReviewOpen = TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion);

    firstTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewOpen);

    when(technicalReviewService.getTechnicalReviewsByApplication(requestApplicationVersion.getApplication()))
        .thenReturn(Collections.singletonList(technicalReviewOpen));

    var technicalReviewRequestAuditEvent = mockTechnicalReviewAuditOfTypeAdd(technicalReviewOpen);
    when(fieldConsentsAuditService.getAuditsFor(
        eq(TechnicalReview.class),
        technicalReviewIdFunctionCaptor.capture(),
        technicalReviewCollectionCaptor.capture()
    )).thenReturn(List.of(technicalReviewRequestAuditEvent));

    var caseEvents = technicalReviewCaseEventService.getCaseEvents(requestApplicationVersion.getApplication());

    assertThat(caseEvents).containsExactly(firstTechnicalReviewRequestedEvent);
    assertThat(technicalReviewIdFunctionCaptor.getValue().apply(technicalReviewOpen)).isEqualTo(TECHNICAL_REVIEW_ID_1);
    assertThat(technicalReviewCollectionCaptor.getValue()).extracting(TechnicalReview::getId).containsExactly(TECHNICAL_REVIEW_ID_1);
  }

  @Test
  void getCaseEvents_withNoOpenAuditRequestAndNoMigratedCases_thenShowAuditEventFromActualData() {
    var technicalReviewOpen = TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion);

    firstTechnicalReviewRequestedEvent = CaseHistoryEventTestUtil
        .getCaseEventForTechnicalReviewRequested(technicalReviewOpen);

    when(technicalReviewService.getTechnicalReviewsByApplication(requestApplicationVersion.getApplication()))
        .thenReturn(Collections.singletonList(technicalReviewOpen));

    var technicalReviewRequestAuditEvent = mockTechnicalReviewAuditOfTypeModify(technicalReviewOpen);
    when(fieldConsentsAuditService.getAuditsFor(
        eq(TechnicalReview.class),
        technicalReviewIdFunctionCaptor.capture(),
        technicalReviewCollectionCaptor.capture()
    )).thenReturn(List.of(technicalReviewRequestAuditEvent));

    var caseEvents = technicalReviewCaseEventService.getCaseEvents(requestApplicationVersion.getApplication());

    assertThat(caseEvents).containsExactly(firstTechnicalReviewRequestedEvent);
    assertThat(technicalReviewIdFunctionCaptor.getValue().apply(technicalReviewOpen)).isEqualTo(TECHNICAL_REVIEW_ID_1);
    assertThat(technicalReviewCollectionCaptor.getValue()).extracting(TechnicalReview::getId).containsExactly(TECHNICAL_REVIEW_ID_1);
  }

  @Test
  void getCaseEvents() {
    when(technicalReviewService.getTechnicalReviewsByApplication(requestApplicationVersion.getApplication()))
        .thenReturn(List.of(technicalReviewRejected, technicalReviewApproved));

    var caseEvents = technicalReviewCaseEventService.getCaseEvents(requestApplicationVersion.getApplication());

    assertThat(caseEvents)
        .containsExactlyInAnyOrder(
            firstTechnicalReviewRequestedEvent,
            firstTechnicalReviewCompletedEvent,
            secondTechnicalReviewRequestedEvent,
            secondTechnicalReviewCompletedEvent
        );
  }

  @Test
  void getCaseEvents_withoutReassignedTechnicalReviewerEvent() {
    var technicalReviewOpen = TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion);
    var technicalReviewResponded = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        requestApplicationVersion,
        TechnicalReviewResponseType.APPROVE
    );

    when(technicalReviewService.getTechnicalReviewsByApplication(requestApplicationVersion.getApplication()))
        .thenReturn(List.of(technicalReviewResponded));

    var technicalReviewRequestAuditEvent = mockTechnicalReviewAuditOfTypeAdd(technicalReviewOpen);
    var technicalReviewResponseAuditEvent = mockTechnicalReviewAuditOfTypeModify(technicalReviewResponded);

    when(fieldConsentsAuditService.getAuditsFor(
        eq(TechnicalReview.class),
        technicalReviewIdFunctionCaptor.capture(),
        technicalReviewCollectionCaptor.capture()
    )).thenReturn(List.of(technicalReviewRequestAuditEvent, technicalReviewResponseAuditEvent));

    doReturn(Optional.empty())
        .when(technicalReviewCaseEventService)
        .getTechnicalReviewReassignedEvent(requestApplicationVersion, technicalReviewRequestAuditEvent, technicalReviewResponseAuditEvent);

    var caseEvents = technicalReviewCaseEventService.getCaseEvents(requestApplicationVersion.getApplication());

    assertThat(caseEvents).hasSize(2);
    assertThat(technicalReviewIdFunctionCaptor.getValue().apply(technicalReviewResponded)).isEqualTo(TECHNICAL_REVIEW_ID_1);
    assertThat(technicalReviewCollectionCaptor.getValue()).extracting(TechnicalReview::getId).containsExactly(TECHNICAL_REVIEW_ID_1);

    verify(technicalReviewCaseEventService).getTechnicalReviewReassignedEvent(requestApplicationVersion, technicalReviewRequestAuditEvent, technicalReviewResponseAuditEvent);
  }

  @Test
  void getCaseEvents_withReassignedTechnicalReviewerEvent() {
    var technicalReviewOpen = TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion);
    var technicalReviewResponded = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        requestApplicationVersion,
        TechnicalReviewResponseType.APPROVE
    );

    when(technicalReviewService.getTechnicalReviewsByApplication(requestApplicationVersion.getApplication()))
        .thenReturn(List.of(technicalReviewResponded));

    var technicalReviewRequestAuditEvent = mockTechnicalReviewAuditOfTypeAdd(technicalReviewOpen);
    var technicalReviewerReassignedAuditEvent = mockTechnicalReviewAuditOfTypeModify(technicalReviewOpen);
    var technicalReviewResponseAuditEvent = mockTechnicalReviewAuditOfTypeModify(technicalReviewResponded);

    when(fieldConsentsAuditService.getAuditsFor(
        eq(TechnicalReview.class),
        technicalReviewIdFunctionCaptor.capture(),
        technicalReviewCollectionCaptor.capture()
    )).thenReturn(List.of(technicalReviewRequestAuditEvent, technicalReviewerReassignedAuditEvent, technicalReviewResponseAuditEvent));

    var technicalReviewReassignedEvent = mockTechnicalReviewReassignedEvent(technicalReviewOpen);
    doReturn(Optional.of(technicalReviewReassignedEvent))
        .when(technicalReviewCaseEventService)
        .getTechnicalReviewReassignedEvent(requestApplicationVersion, technicalReviewRequestAuditEvent, technicalReviewerReassignedAuditEvent);

    var allCaseEvents = technicalReviewCaseEventService.getCaseEvents(requestApplicationVersion.getApplication());
    assertThat(allCaseEvents).hasSize(3);

    var technicalReviewReassignedCaseEvent = allCaseEvents.stream()
        .filter(caseEvent -> TECHNICAL_REVIEW_REASSIGNED.equals(caseEvent.eventType()))
        .findFirst();

    assertThat(technicalReviewReassignedCaseEvent).contains(technicalReviewReassignedEvent);
    assertThat(technicalReviewIdFunctionCaptor.getValue().apply(technicalReviewResponded)).isEqualTo(TECHNICAL_REVIEW_ID_1);
    assertThat(technicalReviewCollectionCaptor.getValue()).extracting(TechnicalReview::getId).containsExactly(TECHNICAL_REVIEW_ID_1);

    verify(technicalReviewCaseEventService).getTechnicalReviewReassignedEvent(requestApplicationVersion, technicalReviewRequestAuditEvent, technicalReviewerReassignedAuditEvent);
  }

  @Test
  void getTechnicalReviewReassignedEvent() {
    var previousTechnicalReviewAuditEntry = new FieldConsentsAudit<>(TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion, TECHNICAL_REVIEWER_USER_1, CLOCK), auditRevision, MOD);
    var currentTechnicalReviewAuditEntry = new FieldConsentsAudit<>(TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion, TECHNICAL_REVIEWER_USER_2, CLOCK), auditRevision, MOD);

    assertThat(technicalReviewCaseEventService.getTechnicalReviewReassignedEvent(requestApplicationVersion, previousTechnicalReviewAuditEntry, currentTechnicalReviewAuditEntry))
        .isPresent()
        .get()
        .extracting(
            CaseEvent::eventType,
            CaseEvent::eventDateTime,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            TECHNICAL_REVIEW_REASSIGNED,
            currentTechnicalReviewAuditEntry.auditRevision().getCreatedDateTime().toInstant(),
            currentTechnicalReviewAuditEntry.auditRevision().getUserWuaId(),
            currentTechnicalReviewAuditEntry.entity().getTechnicalReviewerWuaId(),
            null
        );
  }

  @Test
  void getResponderReassignedEvent_wrongEventType() {
    var previousTechnicalReviewAuditEntry = new FieldConsentsAudit<>(TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion, TECHNICAL_REVIEWER_USER_1, CLOCK), auditRevision, MOD);
    var currentTechnicalReviewAuditEntry = new FieldConsentsAudit<>(TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion, TECHNICAL_REVIEWER_USER_2, CLOCK), auditRevision, ADD);

    assertThat(technicalReviewCaseEventService.getTechnicalReviewReassignedEvent(requestApplicationVersion, previousTechnicalReviewAuditEntry, currentTechnicalReviewAuditEntry)).isEmpty();
  }

  @Test
  void getResponderReassignedEvent_sameResponderWuaId() {
    var previousTechnicalReviewAuditEntry = new FieldConsentsAudit<>(TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion, TECHNICAL_REVIEWER_USER_1, CLOCK), auditRevision, MOD);
    var currentTechnicalReviewAuditEntry = new FieldConsentsAudit<>(TechnicalReviewTestUtil.getOpenTechnicalReview(requestApplicationVersion, TECHNICAL_REVIEWER_USER_1, CLOCK), auditRevision, MOD);

    assertThat(technicalReviewCaseEventService.getTechnicalReviewReassignedEvent(requestApplicationVersion, previousTechnicalReviewAuditEntry, currentTechnicalReviewAuditEntry)).isEmpty();
  }

  private FieldConsentsAudit<TechnicalReview> mockTechnicalReviewAuditOfTypeAdd(TechnicalReview technicalReview) {
    var audit = mock(FieldConsentsAudit.class);
    when(audit.entity()).thenReturn(technicalReview);
    when(audit.revisionType()).thenReturn(RevisionType.ADD);
    return audit;
  }

  private FieldConsentsAudit<TechnicalReview> mockTechnicalReviewAuditOfTypeModify(TechnicalReview technicalReview) {
    var audit = mock(FieldConsentsAudit.class);
    when(audit.entity()).thenReturn(technicalReview);
    when(audit.revisionType()).thenReturn(RevisionType.MOD);
    return audit;
  }

  private CaseEvent mockTechnicalReviewReassignedEvent(TechnicalReview technicalReview) {
    return CaseEvent
        .builder(technicalReview.getRequestApplicationVersion())
        .withEventType(TECHNICAL_REVIEW_REASSIGNED)
        .withMainEventUserWuaId(technicalReview.getTechnicalReviewerWuaId())
        .withEventDateTime(technicalReview.getDeadlineDateTime())
        .withEventText(technicalReview.getRequestText())
        .build();
  }
}
