package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAudit;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;

@ExtendWith(MockitoExtension.class)
class ConsentIssuingEventServiceTest {

  private static final long AUDIT_REVISION_ID = 1L;
  private static final long AUDIT_CREATED_BY_WUA_ID = 1L;

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());
  private static final Instant REVISION_TIMESTAMP = CLOCK.instant().truncatedTo(ChronoUnit.MILLIS);

  @Mock
  private ConsentService consentService;

  @Mock
  private FieldConsentsAuditService fieldConsentsAuditService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Captor
  private ArgumentCaptor<Collection<Integer>> lookupPropertyValuesCaptor;

  @Captor
  private ArgumentCaptor<Function<Consent, Object>> idFunctionCaptor;

  @Spy
  @InjectMocks
  private ConsentIssuingEventService consentIssuingEventService;

  private ApplicationVersion applicationVersion;
  private Application application;
  private AuditRevision auditRevision;
  private Consent consent;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    application = applicationVersion.getApplication();

    auditRevision = new AuditRevision();
    auditRevision.setId(AUDIT_REVISION_ID);
    auditRevision.setCreatedDateTime(Date.from(REVISION_TIMESTAMP));
    auditRevision.setUserWuaId(AUDIT_CREATED_BY_WUA_ID);

    consent = new Consent();
    consent.setApplication(application);
    consent.setIssuedByWuaId(AUDIT_CREATED_BY_WUA_ID);
    consent.setIssuedInstant(REVISION_TIMESTAMP);
  }

  @Test
  void getCaseEvents_whenNoConsentIssuedAudits_andNoDataAvailable() {
    when(fieldConsentsAuditService.getAuditsFor(
        eq(Consent.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(Collections.emptyList());
    when(consentService.findConsent(application)).thenReturn(Optional.empty());

    assertThat(consentIssuingEventService.getCaseEvents(application)).isEmpty();
  }

  @Test
  void getCaseEvents_whenNoConsentIssuedAudits_andDataAvailable() {
    when(fieldConsentsAuditService.getAuditsFor(
        eq(Consent.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(Collections.emptyList());

    when(consentService.findConsent(application)).thenReturn(Optional.of(consent));
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);

    var caseEvent = getConsentIssuedEvent();

    assertThat(consentIssuingEventService.getCaseEvents(application)).containsExactly(caseEvent);
    assertThat(lookupPropertyValuesCaptor.getValue()).extracting(Integer::intValue).containsExactly(application.getId());
    assertThat(idFunctionCaptor.getValue().apply(consent)).isEqualTo(consent.getId());
  }

  @Test
  void getCaseEvents_whenConsentIssuedAudit() {
    var consentIssuedAudit = new FieldConsentsAudit<>(consent, auditRevision, RevisionType.ADD);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);

    when(fieldConsentsAuditService.getAuditsFor(
        eq(Consent.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(List.of(consentIssuedAudit));

    var caseEvent = getConsentIssuedEvent();

    assertThat(consentIssuingEventService.getCaseEvents(application)).containsExactly(caseEvent);
    assertThat(lookupPropertyValuesCaptor.getValue()).extracting(Integer::intValue).containsExactly(application.getId());
    assertThat(idFunctionCaptor.getValue().apply(consent)).isEqualTo(consent.getId());
  }

  @Test
  void getCaseEvents_whenNonAddRevisionTypeAudit_noEventIsReported() {
    var modifiedAudit = new FieldConsentsAudit<>(consent, auditRevision, RevisionType.MOD);
    var deletedAudit = new FieldConsentsAudit<>(consent, auditRevision, RevisionType.DEL);

    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId())).thenReturn(applicationVersion);
    when(fieldConsentsAuditService.getAuditsFor(
        eq(Consent.class),
        idFunctionCaptor.capture(),
        lookupPropertyValuesCaptor.capture(),
        eq("application_id")
    )).thenReturn(List.of(modifiedAudit, deletedAudit));

    assertThat(consentIssuingEventService.getCaseEvents(application)).isEmpty();
    assertThat(lookupPropertyValuesCaptor.getValue()).extracting(Integer::intValue).containsExactly(application.getId());
    assertThat(idFunctionCaptor.getValue().apply(consent)).isEqualTo(consent.getId());
  }

  private CaseEvent getConsentIssuedEvent() {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.CONSENT_ISSUED)
        .withMainEventUserWuaId(AUDIT_CREATED_BY_WUA_ID)
        .withEventDateTime(REVISION_TIMESTAMP)
        .build();
  }
}