package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.envers.RevisionType.MOD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REASSIGNED;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAudit;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;

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
  private FieldConsentsAuditService fieldConsentsAuditService;

  @Spy
  @InjectMocks
  private ConsultationEventService consultationEventService;

  private ApplicationVersion requestApplicationVersion;
  private ApplicationVersion responseApplicationVersion;

  private AuditRevision auditRevision;

  @Captor
  private ArgumentCaptor<Collection<Consultation>> consultationCollectionCaptor;

  @Captor
  private ArgumentCaptor<Function<Consultation, Object>> consultationIdFunctionCaptor;

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
  void getCaseEvents_whenAddAndModifyCaseEvents_theseAreAllShownInCaseHistory() {
    var application = requestApplicationVersion.getApplication();

    var consultation = new Consultation();
    consultation.setId(CONSULTATION_ID);
    consultation.setRequestApplicationVersion(requestApplicationVersion);
    consultation.setResponseApplicationVersion(responseApplicationVersion);

    when(consultationService.getConsultationsByApplication(application)).thenReturn(
        Collections.singletonList(consultation));

    var consultationRequestedAuditEvent = mockConsultationAuditOfTypeAdd(consultation);
    var consultationAssignedAuditEvent = mockConsultationAuditOfTypeModify(consultation);
    var consultationReassignedAuditEvent = mockConsultationAuditOfTypeModify(consultation);
    when(fieldConsentsAuditService.getAuditsFor(
        eq(Consultation.class),
        consultationIdFunctionCaptor.capture(),
        consultationCollectionCaptor.capture()
    )).thenReturn(List.of(consultationRequestedAuditEvent, consultationAssignedAuditEvent, consultationReassignedAuditEvent));

    var caseEvent = mock(CaseEvent.class);
    doReturn(Collections.singletonList(caseEvent))
        .when(consultationEventService)
        .getConsultationAssignmentCaseEvents(eq(responseApplicationVersion), any(), any());

    assertThat(consultationEventService.getCaseEvents(application)).hasSize(3);
    assertThat(consultationCollectionCaptor.getValue()).extracting(Consultation::getId).containsExactly(CONSULTATION_ID);
    assertThat(consultationIdFunctionCaptor.getValue().apply(consultation)).isEqualTo(CONSULTATION_ID);

    verify(consultationEventService).getConsultationAssignmentCaseEvents(requestApplicationVersion, consultationRequestedAuditEvent, consultationAssignedAuditEvent);
    verify(consultationEventService).getConsultationAssignmentCaseEvents(requestApplicationVersion, consultationAssignedAuditEvent, consultationReassignedAuditEvent);
  }

  @Test
  void getCaseEvents_withAddModifyAndDeleteCaseEvents_onlyAddAndModifyAreShownInCaseHistory() {
    var application = requestApplicationVersion.getApplication();

    var consultation = new Consultation();
    consultation.setId(CONSULTATION_ID);
    consultation.setRequestApplicationVersion(requestApplicationVersion);
    consultation.setResponseApplicationVersion(responseApplicationVersion);

    when(consultationService.getConsultationsByApplication(application)).thenReturn(
        Collections.singletonList(consultation));

    var consultationRequestedAuditEvent = mockConsultationAuditOfTypeAdd(consultation);
    var consultationAssignedAuditEvent = mockConsultationAuditOfTypeModify(consultation);
    var consultationReassignedAuditEvent = mockConsultationAuditOfTypeModify(consultation);
    var deleteAuditEvent = mockConsultationAuditOfTypeDelete();
    when(fieldConsentsAuditService.getAuditsFor(
        eq(Consultation.class),
        consultationIdFunctionCaptor.capture(),
        consultationCollectionCaptor.capture()
    )).thenReturn(List.of(consultationRequestedAuditEvent, consultationAssignedAuditEvent, consultationReassignedAuditEvent, deleteAuditEvent));

    var caseEvent = mock(CaseEvent.class);
    doReturn(Collections.singletonList(caseEvent))
        .when(consultationEventService)
        .getConsultationAssignmentCaseEvents(eq(requestApplicationVersion), any(), any());

    assertThat(consultationEventService.getCaseEvents(application)).hasSize(3);
    assertThat(consultationCollectionCaptor.getValue()).extracting(Consultation::getId).containsExactly(CONSULTATION_ID);
    assertThat(consultationIdFunctionCaptor.getValue().apply(consultation)).isEqualTo(CONSULTATION_ID);

    verify(consultationEventService).getConsultationAssignmentCaseEvents(requestApplicationVersion, consultationRequestedAuditEvent, consultationAssignedAuditEvent);
    verify(consultationEventService).getConsultationAssignmentCaseEvents(requestApplicationVersion, consultationAssignedAuditEvent, consultationReassignedAuditEvent);
  }

  @Test
  void getCaseEvents_noAudits() {
    var application = requestApplicationVersion.getApplication();
    when(consultationService.getConsultationsByApplication(application))
        .thenReturn(Collections.emptyList());

    when(fieldConsentsAuditService.getAuditsFor(
        eq(Consultation.class),
        consultationIdFunctionCaptor.capture(),
        consultationCollectionCaptor.capture()
    )).thenReturn(Collections.emptyList());

    assertThat(consultationEventService.getCaseEvents(application)).isEmpty();
  }

  @Test
  void getConsultationCaseEvents() {
    FieldConsentsAudit<Consultation> previous = mock(FieldConsentsAudit.class);
    FieldConsentsAudit<Consultation> current = mock(FieldConsentsAudit.class);

    var assignedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(assignedEvent))
        .when(consultationEventService)
        .getResponderAssignedEvent(requestApplicationVersion, previous, current);

    var reAssignedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(reAssignedEvent))
        .when(consultationEventService)
        .getResponderReassignedEvent(requestApplicationVersion, previous, current);

    assertThat(consultationEventService.getConsultationAssignmentCaseEvents(requestApplicationVersion, previous, current))
        .containsExactly(
            assignedEvent,
            reAssignedEvent
        );
  }

  @Test
  void getConsultationCaseEvents_notReassigned() {
    FieldConsentsAudit<Consultation> previous = mock(FieldConsentsAudit.class);
    FieldConsentsAudit<Consultation> current = mock(FieldConsentsAudit.class);

    var assignedEvent = mock(CaseEvent.class);
    doReturn(Optional.of(assignedEvent))
        .when(consultationEventService)
        .getResponderAssignedEvent(requestApplicationVersion, previous, current);

    doReturn(Optional.empty())
        .when(consultationEventService)
        .getResponderReassignedEvent(requestApplicationVersion, previous, current);

    assertThat(consultationEventService.getConsultationAssignmentCaseEvents(requestApplicationVersion, previous, current))
        .containsExactly(
            assignedEvent
        );
  }

  @Test
  void getResponderAssignedEvent() {
    var previous = new FieldConsentsAudit<>(createAuditedConsultationForResponder(null), auditRevision, RevisionType.ADD);
    var current = new FieldConsentsAudit<>(createAuditedConsultationForResponder(1L), auditRevision, MOD);

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
            current.entity().getResponderWuaId(),
            null
        );
  }

  @Test
  void getResponderAssignedEvent_assignerAlreadyExists() {
    var previous = new FieldConsentsAudit<>(createAuditedConsultationForResponder(1L), auditRevision, RevisionType.ADD);
    var current = new FieldConsentsAudit<>(createAuditedConsultationForResponder(1L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderAssignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponderAssignedEvent_assignerChanged() {
    var previous = new FieldConsentsAudit<>(createAuditedConsultationForResponder(1L), auditRevision, RevisionType.ADD);
    var current = new FieldConsentsAudit<>(createAuditedConsultationForResponder(2L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderAssignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponderReassignedEvent() {
    var previous = new FieldConsentsAudit<>(createAuditedConsultationForResponder(1L), auditRevision, MOD);
    var current = new FieldConsentsAudit<>(createAuditedConsultationForResponder(2L), auditRevision, MOD);

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
            current.entity().getResponderWuaId(),
            null
        );
  }

  @Test
  void getResponderReassignedEvent_noPreviousResponder() {
    var previous = new FieldConsentsAudit<>(createAuditedConsultationForResponder(null), auditRevision, MOD);
    var current = new FieldConsentsAudit<>(createAuditedConsultationForResponder(2L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderReassignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponderReassignedEvent_wrongEventType() {
    var previous = new FieldConsentsAudit<>(createAuditedConsultationForResponder(1L), auditRevision, MOD);
    var current = new FieldConsentsAudit<>(createAuditedConsultationForResponder(2L), auditRevision, RevisionType.ADD);

    assertThat(consultationEventService.getResponderReassignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  @Test
  void getResponderReassignedEvent_sameResponderWuaId() {
    var previous = new FieldConsentsAudit<>(createAuditedConsultationForResponder(1L), auditRevision, MOD);
    var current = new FieldConsentsAudit<>(createAuditedConsultationForResponder(1L), auditRevision, MOD);

    assertThat(consultationEventService.getResponderReassignedEvent(requestApplicationVersion, previous, current)).isEmpty();
  }

  private Consultation createAuditedConsultationForResponder(Long responderWuaId) {
    var consultation = new Consultation();

    consultation.setId(CONSULTATION_ID);
    consultation.setRequestDeadline(REQUEST_DEADLINE);
    consultation.setResponderWuaId(responderWuaId);

    return consultation;
  }

  private FieldConsentsAudit<Consultation> mockConsultationAuditOfTypeAdd(Consultation consultation) {
    var audit = mock(FieldConsentsAudit.class);
    when(audit.entity()).thenReturn(consultation);
    when(audit.revisionType()).thenReturn(RevisionType.ADD);
    when(audit.auditRevision()).thenReturn(auditRevision);
    return audit;
  }

  private FieldConsentsAudit<Consultation> mockConsultationAuditOfTypeModify(Consultation consultation) {
    var audit = mock(FieldConsentsAudit.class);
    when(audit.entity()).thenReturn(consultation);
    when(audit.revisionType()).thenReturn(RevisionType.MOD);
    return audit;
  }

  private FieldConsentsAudit<Consultation> mockConsultationAuditOfTypeDelete() {
    var audit = mock(FieldConsentsAudit.class);
    when(audit.revisionType()).thenReturn(RevisionType.DEL);
    return audit;
  }
}
