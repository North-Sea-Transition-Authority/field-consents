package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.envers.RevisionType.MOD;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REQUESTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_RESPONDED;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class ConsultationEventServiceTest {

  private static final int CONSULTATION_ID = 1;
  private static final long AUDIT_REVISION_ID = 1L;
  private static final long AUDIT_CREATED_BY_WUA_ID = 1L;

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private static final Instant REVISION_TIMESTAMP = CLOCK.instant().truncatedTo(ChronoUnit.MILLIS);
  private static final Instant REQUEST_DEADLINE = CLOCK.instant().plus(5, ChronoUnit.DAYS);

  @Mock
  private ConsultationService consultationService;

  @Mock
  private ConsultationAuditService consultationAuditService;

  @Spy
  @InjectMocks
  private ConsultationEventService consultationEventService;

  private ApplicationVersion requestApplicationVersion;
  private ApplicationVersion responseApplicationVersion;

  private AuditRevision auditRevision;

  @Captor
  private ArgumentCaptor<Collection<Consultation>> consultationCollectionCaptor;

  @BeforeEach
  void setUp() {
    requestApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    responseApplicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    responseApplicationVersion.setVersion(2);

    auditRevision = new AuditRevision();
    auditRevision.setId(AUDIT_REVISION_ID);
    auditRevision.setCreatedDateTime(Date.from(REVISION_TIMESTAMP));
    auditRevision.setUserWuaId(AUDIT_CREATED_BY_WUA_ID);
  }

  @Test
  void getCaseEvents() {
    var application = requestApplicationVersion.getApplication();

    var consultation = new Consultation();
    consultation.setId(CONSULTATION_ID);
    consultation.setRequestApplicationVersion(requestApplicationVersion);
    consultation.setResponseApplicationVersion(responseApplicationVersion);

    when(consultationService.getConsultationsByApplication(application)).thenReturn(
        Collections.singletonList(consultation));

    var audit1 = mockConsultationAudit(consultation);
    var audit2 = mockConsultationAudit(consultation);
    var audit3 = mockConsultationAudit(consultation);
    when(consultationAuditService.getConsultationAudits(consultationCollectionCaptor.capture()))
        .thenReturn(List.of(audit1, audit2, audit3));

    var caseEvent = mock(CaseEvent.class);
    doReturn(Collections.singletonList(caseEvent))
        .when(consultationEventService)
        .getConsultationCaseEvents(eq(requestApplicationVersion), eq(responseApplicationVersion), any(), any());

    assertThat(consultationEventService.getCaseEvents(application)).hasSize(3).allMatch(caseEvent::equals);

    assertThat(consultationCollectionCaptor.getValue()).extracting(Consultation::getId).containsExactly(CONSULTATION_ID);
    verify(consultationEventService).getConsultationCaseEvents(requestApplicationVersion, responseApplicationVersion, null, audit1);
    verify(consultationEventService).getConsultationCaseEvents(requestApplicationVersion, responseApplicationVersion, audit1, audit2);
    verify(consultationEventService).getConsultationCaseEvents(requestApplicationVersion, responseApplicationVersion, audit2, audit3);
  }

  @Test
  void getCaseEvents_noAudits() {
    var application = requestApplicationVersion.getApplication();

    var consultation = new Consultation();
    consultation.setId(CONSULTATION_ID);
    consultation.setRequestApplicationVersion(requestApplicationVersion);

    when(consultationService.getConsultationsByApplication(application))
        .thenReturn(Collections.singletonList(consultation));

    when(consultationAuditService.getConsultationAudits(consultationCollectionCaptor.capture()))
        .thenReturn(Collections.emptyList());

    assertThat(consultationEventService.getCaseEvents(application)).isEmpty();

    assertThat(consultationCollectionCaptor.getValue()).extracting(Consultation::getId).containsExactly(CONSULTATION_ID);
  }

  @Test
  void getConsultationCaseEvents() {
    var previous = mock(ConsultationAudit.class);
    var current = mock(ConsultationAudit.class);

    var requestedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(requestedEvent))
        .when(consultationEventService)
        .getRequestedEvent(requestApplicationVersion, current);

    var assignedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(assignedEvent))
        .when(consultationEventService)
        .getResponderAssignedEvent(requestApplicationVersion, previous, current);

    var reAssignedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(reAssignedEvent))
        .when(consultationEventService)
        .getResponderReassignedEvent(requestApplicationVersion, previous, current);

    var respondedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(respondedEvent))
        .when(consultationEventService)
        .getResponseSubmittedEvent(requestApplicationVersion, previous, current);

    assertThat(consultationEventService.getConsultationCaseEvents(requestApplicationVersion, responseApplicationVersion, previous, current))
        .containsExactly(
            requestedEvent,
            assignedEvent,
            reAssignedEvent,
            respondedEvent
        );
  }

  @Test
  void getConsultationCaseEvents_notReassigned() {
    var previous = mock(ConsultationAudit.class);
    var current = mock(ConsultationAudit.class);

    var requestedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(requestedEvent))
        .when(consultationEventService)
        .getRequestedEvent(requestApplicationVersion, current);

    var assignedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(assignedEvent))
        .when(consultationEventService)
        .getResponderAssignedEvent(requestApplicationVersion, previous, current);

    doReturn(Optional.empty())
        .when(consultationEventService)
        .getResponderReassignedEvent(requestApplicationVersion, previous, current);

    var respondedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(respondedEvent))
        .when(consultationEventService)
        .getResponseSubmittedEvent(requestApplicationVersion, previous, current);

    assertThat(consultationEventService.getConsultationCaseEvents(requestApplicationVersion, responseApplicationVersion, previous, current))
        .containsExactly(
            requestedEvent,
            assignedEvent,
            respondedEvent
        );
  }

  @Test
  void getConsultationCaseEvents_notResponded() {
    var previous = mock(ConsultationAudit.class);
    var current = mock(ConsultationAudit.class);

    var requestedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(requestedEvent))
        .when(consultationEventService)
        .getRequestedEvent(requestApplicationVersion, current);

    var assignedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(assignedEvent))
        .when(consultationEventService)
        .getResponderAssignedEvent(requestApplicationVersion, previous, current);

    var reAssignedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(reAssignedEvent))
        .when(consultationEventService)
        .getResponderReassignedEvent(requestApplicationVersion, previous, current);

    doReturn(Optional.empty())
        .when(consultationEventService)
        .getResponseSubmittedEvent(requestApplicationVersion, previous, current);

    assertThat(consultationEventService.getConsultationCaseEvents(requestApplicationVersion, responseApplicationVersion, previous, current))
        .containsExactly(
            requestedEvent,
            assignedEvent,
            reAssignedEvent
        );
  }

  @Test
  void getConsultationCaseEvents_previousIsNull() {
    var current = mock(ConsultationAudit.class);
    var caseEvent = mock(CaseEvent.class);

    doReturn(Optional.of(caseEvent)).when(consultationEventService).getRequestedEvent(requestApplicationVersion, current);

    assertThat(consultationEventService.getConsultationCaseEvents(requestApplicationVersion, responseApplicationVersion, null, current))
        .containsExactly(caseEvent);
  }

  @Test
  void getRequestedEvent() {
    var current = new ConsultationAudit(createAuditedConsultationForResponder(null), auditRevision, RevisionType.ADD);

    assertThat(consultationEventService.getRequestedEvent(requestApplicationVersion, current))
        .isPresent()
        .get()
        .extracting(
            CaseEvent::eventType,
            CaseEvent::eventDateTime,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CONSULTATION_REQUESTED,
            current.auditRevision().getCreatedDateTime().toInstant(),
            current.auditRevision().getUserWuaId(),
            null,
            DateUtils.format(REQUEST_DEADLINE, DateUtils.DATE_TIME)
        );
  }

  @ParameterizedTest
  @EnumSource(value = RevisionType.class, names = "ADD", mode = EXCLUDE)
  void getRequestedEvent_invalidRevisionType(RevisionType revisionType) {
    var current = new ConsultationAudit(createAuditedConsultationForResponder(null), auditRevision, revisionType);
    assertThat(consultationEventService.getRequestedEvent(requestApplicationVersion, current)).isEmpty();
  }

  @Test
  void getResponderAssignedEvent() {
    var previous = new ConsultationAudit(createAuditedConsultationForResponder(null), auditRevision, RevisionType.ADD);
    var current = new ConsultationAudit(createAuditedConsultationForResponder(1L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderAssignedEvent(requestApplicationVersion, previous, current))
        .isPresent()
        .get()
        .extracting(
            CaseEvent::eventType,
            CaseEvent::eventDateTime,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CONSULTATION_ASSIGNED,
            current.auditRevision().getCreatedDateTime().toInstant(),
            current.auditRevision().getUserWuaId(),
            current.consultation().getResponderWuaId(),
            null
        );
  }

  @Test
  void getResponderAssignedEvent_assignerAlreadyExists() {
    var previous = new ConsultationAudit(createAuditedConsultationForResponder(1L), auditRevision, RevisionType.ADD);
    var current = new ConsultationAudit(createAuditedConsultationForResponder(1L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderAssignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponderAssignedEvent_assignerChanged() {
    var previous = new ConsultationAudit(createAuditedConsultationForResponder(1L), auditRevision, RevisionType.ADD);
    var current = new ConsultationAudit(createAuditedConsultationForResponder(2L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderAssignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponderReassignedEvent() {
    var previous = new ConsultationAudit(createAuditedConsultationForResponder(1L), auditRevision, MOD);
    var current = new ConsultationAudit(createAuditedConsultationForResponder(2L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderReassignedEvent(requestApplicationVersion, previous, current))
        .isPresent()
        .get()
        .extracting(
            CaseEvent::eventType,
            CaseEvent::eventDateTime,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CONSULTATION_REASSIGNED,
            current.auditRevision().getCreatedDateTime().toInstant(),
            current.auditRevision().getUserWuaId(),
            current.consultation().getResponderWuaId(),
            null
        );
  }

  @Test
  void getResponderReassignedEvent_noPreviousResponder() {
    var previous = new ConsultationAudit(createAuditedConsultationForResponder(null), auditRevision, MOD);
    var current = new ConsultationAudit(createAuditedConsultationForResponder(2L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderReassignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponderReassignedEvent_wrongEventType() {
    var previous = new ConsultationAudit(createAuditedConsultationForResponder(1L), auditRevision, MOD);
    var current = new ConsultationAudit(createAuditedConsultationForResponder(2L), auditRevision, RevisionType.ADD);

    assertThat(consultationEventService.getResponderReassignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponderReassignedEvent_sameResponderWuaId() {
    var previous = new ConsultationAudit(createAuditedConsultationForResponder(1L), auditRevision, MOD);
    var current = new ConsultationAudit(createAuditedConsultationForResponder(1L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderReassignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponseSubmittedEvent() {
    var previous = new ConsultationAudit(createAuditedConsultationForRespondedBy(null), auditRevision,
        MOD);
    var current = new ConsultationAudit(createAuditedConsultationForRespondedBy(1L), auditRevision, MOD);

    assertThat(consultationEventService.getResponseSubmittedEvent(requestApplicationVersion, previous, current))
        .isPresent()
        .get()
        .extracting(
            CaseEvent::eventType,
            CaseEvent::eventDateTime,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::otherEventUserWuaId,
            CaseEvent::eventText
        ).containsExactly(
            CONSULTATION_RESPONDED,
            current.auditRevision().getCreatedDateTime().toInstant(),
            current.consultation().getRespondedByWuaId(),
            null,
            null
        );
  }

  @Test
  void getResponseSubmittedEvent_sameResponderWuaId() {
    var previous = new ConsultationAudit(createAuditedConsultationForRespondedBy(1L), auditRevision, MOD);
    var current = new ConsultationAudit(createAuditedConsultationForRespondedBy(1L), auditRevision, MOD);

    assertThat(consultationEventService.getResponseSubmittedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  private Consultation createAuditedConsultationForResponder(Long responderWuaId) {
    var consultation = new Consultation();

    consultation.setId(CONSULTATION_ID);
    consultation.setRequestDeadline(REQUEST_DEADLINE);
    consultation.setResponderWuaId(responderWuaId);

    return consultation;
  }

  private Consultation createAuditedConsultationForRespondedBy(Long respondedByWuaId) {
    var consultation = new Consultation();

    consultation.setId(CONSULTATION_ID);
    consultation.setRequestDeadline(REQUEST_DEADLINE);
    consultation.setResponderWuaId(1L);
    consultation.setRespondedByWuaId(respondedByWuaId);

    return consultation;
  }

  private ConsultationAudit mockConsultationAudit(Consultation consultation) {
    var audit = mock(ConsultationAudit.class);
    when(audit.consultation()).thenReturn(consultation);
    return audit;
  }

}
