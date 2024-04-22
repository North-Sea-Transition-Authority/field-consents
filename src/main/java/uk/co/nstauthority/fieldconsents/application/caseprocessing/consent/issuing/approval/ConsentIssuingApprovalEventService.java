package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.APPROVED_FOR_ISSUE;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.UNAPPROVED_FOR_ISSUE;

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
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAudit;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;

@Service
public class ConsentIssuingApprovalEventService implements CaseEventService<Application> {

  private final FieldConsentsAuditService auditService;
  private final ApplicationVersionService applicationVersionService;

  public ConsentIssuingApprovalEventService(FieldConsentsAuditService auditService,
                                            ApplicationVersionService applicationVersionService) {
    this.auditService = auditService;
    this.applicationVersionService = applicationVersionService;
  }
  
  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();

    var consentIssuingApprovalAudits = auditService.getAuditsFor(
        ConsentIssuingApproval.class,
        ConsentIssuingApproval::getId,
        Set.of(application.getId()),
        "application_id");

    if (consentIssuingApprovalAudits.isEmpty()) {
      return Collections.emptyList();
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId());

    for (FieldConsentsAudit<ConsentIssuingApproval> consentIssuingApprovalAudit : consentIssuingApprovalAudits) {

      if (consentIssuingApprovalAudit.revisionType().equals(RevisionType.ADD)) {
        caseEvents.add(
            getConsentIssuingApprovalEvent(consentIssuingApprovalAudit, APPROVED_FOR_ISSUE, applicationVersion)
        );
      }

      if (consentIssuingApprovalAudit.revisionType().equals(RevisionType.DEL)) {
        caseEvents.add(
            getConsentIssuingApprovalEvent(consentIssuingApprovalAudit, UNAPPROVED_FOR_ISSUE, applicationVersion)
        );
      }
    }

    return caseEvents;
  }

  private CaseEvent getConsentIssuingApprovalEvent(FieldConsentsAudit<ConsentIssuingApproval> fieldConsentsAudit,
                                                   CaseEventType eventType,
                                                   ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(eventType)
        .withMainEventUserWuaId(fieldConsentsAudit.auditRevision().getUserWuaId())
        .withEventDateTime(fieldConsentsAudit.auditRevision().getCreatedDateTime().toInstant())
        .build();
  }
}
