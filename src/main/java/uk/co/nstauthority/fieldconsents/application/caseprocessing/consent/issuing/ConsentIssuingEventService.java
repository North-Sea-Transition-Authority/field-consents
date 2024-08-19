package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.hibernate.envers.RevisionType;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentService;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAudit;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;

@Service
public class ConsentIssuingEventService implements CaseEventService<Application> {

  private final ConsentService consentService;
  private final FieldConsentsAuditService auditService;
  private final ApplicationVersionService applicationVersionService;

  public ConsentIssuingEventService(ConsentService consentService,
                                    FieldConsentsAuditService auditService,
                                    ApplicationVersionService applicationVersionService) {
    this.consentService = consentService;
    this.auditService = auditService;
    this.applicationVersionService = applicationVersionService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();

    var consentAudits = auditService.getAuditsFor(
        Consent.class,
        Consent::getId,
        Set.of(application.getId()),
        "application_id");

    // migrated cases will have no audit rows, so we fall back to the base application consent data
    if (consentAudits.isEmpty()) {
      var consentOptional = consentService.findConsent(application);

      if (consentOptional.isEmpty()) {
        return Collections.emptyList();
      }

      caseEvents.add(
          getConsentIssuedEvent(
              consentOptional.get(),
              applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
      );
      return caseEvents;
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId());

    // get case events for consent audit data
    consentAudits.stream()
        .filter(audit -> audit.revisionType().equals(RevisionType.ADD))
        .forEach(audit -> caseEvents
            .add(getConsentIssuedEventFromAudit(audit, applicationVersion)));

    return caseEvents;
  }

  private CaseEvent getConsentIssuedEvent(Consent consent, ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.CONSENT_ISSUED)
        .withMainEventUserWuaId(consent.getIssuedByWuaId())
        .withEventDateTime(consent.getIssuedInstant())
        .build();
  }

  private CaseEvent getConsentIssuedEventFromAudit(FieldConsentsAudit<Consent> fieldConsentsAudit,
                                                   ApplicationVersion applicationVersion) {
    return CaseEvent.newBuilderForAuditRevision(
        fieldConsentsAudit.auditRevision(),
        applicationVersion)
        .withEventType(CaseEventType.CONSENT_ISSUED)
        .build();
  }
}
