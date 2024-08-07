package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.BREACH_RECORDED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.BREACH_REMOVED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.hibernate.envers.RevisionType;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;

@Service
public class ConsentBreachEventService implements CaseEventService<Application> {

  private final ApplicationVersionService applicationVersionService;
  private final FieldConsentsAuditService fieldConsentsAuditService;
  private final ConsentService consentService;

  ConsentBreachEventService(
      ApplicationVersionService applicationVersionService,
      FieldConsentsAuditService fieldConsentsAuditService,
      ConsentService consentService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.fieldConsentsAuditService = fieldConsentsAuditService;
    this.consentService = consentService;
  }

  @Override
  public Collection<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();

    var applicationId = application.getId();
    var consent = consentService.findConsent(application);

    if (consent.isEmpty()) {
      return Collections.emptyList();
    }

    var consentBreachAudits = fieldConsentsAuditService.getAuditsFor(
        ConsentBreach.class,
        ConsentBreach::getId,
        Set.of(consent.get().getId()),
        "consent_id");

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(applicationId);

    consentBreachAudits.stream()
        .filter(audit -> !RevisionType.MOD.equals(audit.revisionType()))
        .forEach(audit -> caseEvents
            .add(CaseEvent.newBuilderForAuditRevision(audit.auditRevision(), applicationVersion)
            .withEventType(getEventTypeFromRevisionType(audit.revisionType(), applicationId))
            .build()
        ));
    return caseEvents;
  }

  private CaseEventType getEventTypeFromRevisionType(RevisionType revisionType, Integer applicationId) {
    return switch (revisionType) {
      case ADD -> BREACH_RECORDED;
      case DEL -> BREACH_REMOVED;
      case MOD -> throw new IllegalStateException("Cannot map revisionType %s to an appropriate EventType for %s"
          .formatted(revisionType, applicationId));
    };
  }
}
