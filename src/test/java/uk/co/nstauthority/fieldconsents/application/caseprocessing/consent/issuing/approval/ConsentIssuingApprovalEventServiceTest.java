package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAudit;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;

@ExtendWith(MockitoExtension.class)
class ConsentIssuingApprovalEventServiceTest {

  private static final long AUDIT_REVISION_ID = 1L;
  private static final long AUDIT_CREATED_BY_WUA_ID = 1L;

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private static final Instant REVISION_TIMESTAMP = CLOCK.instant().truncatedTo(ChronoUnit.MILLIS);

  @Mock
  private FieldConsentsAuditService fieldConsentsAuditService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentIssuingApprovalService consentIssuingApprovalService;

  @Captor
  private ArgumentCaptor<Collection<Integer>> lookupPropertyValuesCaptor;

  @Captor
  private ArgumentCaptor<Function<ConsentIssuingApproval, Object>> idFunctionCaptor;

  @Spy
  @InjectMocks
  private ConsentIssuingApprovalEventService consentIssuingApprovalEventService;

  private ApplicationVersion applicationVersion;
  private Application application;
  private AuditRevision auditRevision;
  private ConsentIssuingApproval consentIssuingApproval;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    auditRevision = new AuditRevision();
    auditRevision.setId(AUDIT_REVISION_ID);
    auditRevision.setCreatedDateTime(Date.from(REVISION_TIMESTAMP));
    auditRevision.setUserWuaId(AUDIT_CREATED_BY_WUA_ID);

    consentIssuingApproval = new ConsentIssuingApproval();
    consentIssuingApproval.setApplication(application);
    consentIssuingApproval.setApprovedByWuaId(AUDIT_CREATED_BY_WUA_ID);
    consentIssuingApproval.setApprovedInstant(REVISION_TIMESTAMP);
  }

  @Test
  void getCaseEvents_whenNoConsentIssuingApprovalAudits_andNoDataAvailable() {
    when(fieldConsentsAuditService.getAuditsFor(
        eq(ConsentIssuingApproval.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(Collections.emptyList());
    when(consentIssuingApprovalService.findConsentIssuingApproval(application)).thenReturn(Optional.empty());

    assertThat(consentIssuingApprovalEventService.getCaseEvents(application)).isEmpty();
  }

  @Test
  void getCaseEvents_whenNoConsentIssuingApprovalAudits_andDataAvailable() {
    when(fieldConsentsAuditService.getAuditsFor(
        eq(ConsentIssuingApproval.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(Collections.emptyList());

    when(consentIssuingApprovalService.findConsentIssuingApproval(application)).thenReturn(Optional.of(consentIssuingApproval));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);

    var caseEvent = getApprovedForIssueEvent();

    assertThat(consentIssuingApprovalEventService.getCaseEvents(application)).containsExactly(caseEvent);
    assertThat(lookupPropertyValuesCaptor.getValue()).extracting(Integer::intValue).containsExactly(application.getId());
    assertThat(idFunctionCaptor.getValue().apply(consentIssuingApproval)).isEqualTo(consentIssuingApproval.getId());
  }

  @Test
  void getCaseEvents_whenApprovedForIssueAudit() {
    var approvedForIssueAudit = new FieldConsentsAudit<>(consentIssuingApproval, auditRevision, RevisionType.ADD);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);

    when(fieldConsentsAuditService.getAuditsFor(
        eq(ConsentIssuingApproval.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(List.of(approvedForIssueAudit));

    var caseEvent = getApprovedForIssueEvent();

    assertThat(consentIssuingApprovalEventService.getCaseEvents(application)).containsExactly(caseEvent);
    assertThat(lookupPropertyValuesCaptor.getValue()).extracting(Integer::intValue).containsExactly(application.getId());
    assertThat(idFunctionCaptor.getValue().apply(consentIssuingApproval)).isEqualTo(consentIssuingApproval.getId());
  }

  @Test
  void getCaseEvents_whenApprovedAndUnapprovedForIssueAudit() {
    var approvedForIssueAudit = new FieldConsentsAudit<>(consentIssuingApproval, auditRevision, RevisionType.ADD);
    var unapprovedForIssueAudit = new FieldConsentsAudit<>(consentIssuingApproval, auditRevision, RevisionType.DEL);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);

    when(fieldConsentsAuditService.getAuditsFor(
        eq(ConsentIssuingApproval.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(List.of(approvedForIssueAudit, unapprovedForIssueAudit));

    var approvedForIssueEvent = getApprovedForIssueEvent();
    var unapprovedForIssueEvent = getUnapprovedForIssueEvent();

    assertThat(consentIssuingApprovalEventService.getCaseEvents(application)).containsExactly(approvedForIssueEvent, unapprovedForIssueEvent);
    assertThat(lookupPropertyValuesCaptor.getValue()).extracting(Integer::intValue).containsExactly(application.getId());
    assertThat(idFunctionCaptor.getValue().apply(consentIssuingApproval)).isEqualTo(consentIssuingApproval.getId());
  }

  @Test
  void getCaseEvents_whenModifiedAuditEvent_noEventIsReported() {
    var modifiedAudit = new FieldConsentsAudit<>(consentIssuingApproval, auditRevision, RevisionType.MOD);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);

    when(fieldConsentsAuditService.getAuditsFor(
        eq(ConsentIssuingApproval.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(List.of(modifiedAudit));

    assertThat(consentIssuingApprovalEventService.getCaseEvents(application)).isEmpty();
    assertThat(lookupPropertyValuesCaptor.getValue()).extracting(Integer::intValue).containsExactly(application.getId());
    assertThat(idFunctionCaptor.getValue().apply(consentIssuingApproval)).isEqualTo(consentIssuingApproval.getId());
  }

  private CaseEvent getApprovedForIssueEvent() {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.APPROVED_FOR_ISSUE)
        .withMainEventUserWuaId(AUDIT_CREATED_BY_WUA_ID)
        .withEventDateTime(REVISION_TIMESTAMP)
        .build();
  }

  private CaseEvent getUnapprovedForIssueEvent() {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.UNAPPROVED_FOR_ISSUE)
        .withMainEventUserWuaId(AUDIT_CREATED_BY_WUA_ID)
        .withEventDateTime(REVISION_TIMESTAMP)
        .build();
  }
}
