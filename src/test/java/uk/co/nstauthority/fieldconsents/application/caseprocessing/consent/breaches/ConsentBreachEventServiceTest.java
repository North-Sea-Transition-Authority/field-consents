package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.BREACH_RECORDED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.BREACH_REMOVED;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.hibernate.envers.RevisionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.audit.AuditRevision;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAudit;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsentBreachEventServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private FieldConsentsAuditService fieldConsentsAuditService;

  @Mock
  private ConsentService consentService;

  @InjectMocks
  private ConsentBreachEventService consentBreachEventService;

  @Captor
  private ArgumentCaptor<Function<ConsentBreach, Object>> idFunctionCaptor;

  private final ApplicationVersion applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(
      ApplicationType.FLARE);

  private final ServiceUserDetail user = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void getCaseEvents_noConsent() {
    when(consentService.findConsent(applicationVersion.getApplication()))
        .thenReturn(Optional.empty());
    verify(fieldConsentsAuditService, never())
        .getAuditsFor(
            any(),
            any(),
            anyCollection(),
            any()
        );
    assertThat(consentBreachEventService.getCaseEvents(applicationVersion.getApplication()))
        .isEmpty();
  }

  @Test
  void getCaseEvents_withNoConsentBreaches() {
    var consent = new Consent(10);
    when(consentService.findConsent(applicationVersion.getApplication()))
        .thenReturn(Optional.of(consent));
    when(fieldConsentsAuditService.getAuditsFor(
        eq(ConsentBreach.class),
        idFunctionCaptor.capture(),
        eq(Set.of(consent.getId())),
        eq("consent_id")))
        .thenReturn(Collections.emptyList());
    assertThat(consentBreachEventService.getCaseEvents(applicationVersion.getApplication()))
        .isEmpty();

    var consentBreach = new ConsentBreach(7);
    assertThat(idFunctionCaptor.getValue().apply(consentBreach)).isEqualTo(consentBreach.getId());
  }

  @Test
  void getCaseEvents() {
    var consent = new Consent(10);
    var consentOption = Optional.of(consent);

    var consentBreach = new ConsentBreach(12);
    consentBreach.setConsent(consent);
    consentBreach.setBreachText("initial text");
    consentBreach.setAddedByWuaId(user.wuaId());
    consentBreach.setAddedDateTime(Instant.now());

    var consentBreach2 = new ConsentBreach(12);
    consentBreach2.setConsent(consent);
    consentBreach2.setBreachText("new text");
    consentBreach2.setAddedByWuaId(user.wuaId());
    consentBreach2.setAddedDateTime(Instant.now());

    var createdInstant = Instant.now();
    var createdDateInstant = Date.from(createdInstant).toInstant();
    var createdBreachAuditRevision = new AuditRevision();
    createdBreachAuditRevision.setId(1);
    createdBreachAuditRevision.setCreatedDateTime(Date.from(createdInstant));
    createdBreachAuditRevision.setUserWuaId(user.wuaId());

    var updatedInstant = Instant.now();
    var updatedDateInstant = Date.from(updatedInstant).toInstant();
    var updatedBreachAuditRevision = new AuditRevision();
    updatedBreachAuditRevision.setId(2);
    updatedBreachAuditRevision.setCreatedDateTime(Date.from(updatedDateInstant));
    updatedBreachAuditRevision.setUserWuaId(user.wuaId());

    var deletedInstant = Instant.now();
    var deletedDateInstant = Date.from(deletedInstant).toInstant();
    var deletedBreachAuditRevision = new AuditRevision();
    deletedBreachAuditRevision.setId(3);
    deletedBreachAuditRevision.setCreatedDateTime(Date.from(deletedInstant));
    deletedBreachAuditRevision.setUserWuaId(user.wuaId());

    var fieldConsentAudit1 = new FieldConsentsAudit<>(
        consentBreach,
        createdBreachAuditRevision,
        RevisionType.ADD);
    var fieldConsentAudit2 = new FieldConsentsAudit<>(
        consentBreach2,
        updatedBreachAuditRevision,
        RevisionType.MOD);
    var fieldConsentAudit3 = new FieldConsentsAudit<>(
        consentBreach2,
        deletedBreachAuditRevision,
        RevisionType.DEL);

    var fieldConsentAudits = List.of(fieldConsentAudit1, fieldConsentAudit2, fieldConsentAudit3);

    when(consentService.findConsent(applicationVersion.getApplication()))
        .thenReturn(consentOption);
    when(fieldConsentsAuditService.getAuditsFor(
        eq(ConsentBreach.class),
        any(),
        anyCollection(),
        any()))
        .thenReturn(fieldConsentAudits);

    assertThat(consentBreachEventService.getCaseEvents(applicationVersion.getApplication()))
        .extracting(
            CaseEvent::eventType,
            CaseEvent::mainEventUserWuaId,
            CaseEvent::eventDateTime)
        .containsExactly(
            tuple(BREACH_RECORDED,
                user.wuaId(),
                createdDateInstant),
            tuple(BREACH_REMOVED,
                user.wuaId(),
                deletedDateInstant)
        );
  }
}
