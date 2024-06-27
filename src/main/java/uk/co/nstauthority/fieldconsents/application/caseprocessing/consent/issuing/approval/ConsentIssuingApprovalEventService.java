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
  private final ConsentIssuingApprovalService consentIssuingApprovalService;

  public ConsentIssuingApprovalEventService(FieldConsentsAuditService auditService,
                                            ApplicationVersionService applicationVersionService,
                                            ConsentIssuingApprovalService consentIssuingApprovalService) {
    this.auditService = auditService;
    this.applicationVersionService = applicationVersionService;
    this.consentIssuingApprovalService = consentIssuingApprovalService;
  }
  
  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();

    var consentIssuingApprovalAudits = auditService.getAuditsFor(
        ConsentIssuingApproval.class,
        ConsentIssuingApproval::getId,
        Set.of(application.getId()),
        "application_id");

    // migrated cases will have no audit rows, so we fall back to the base consent issuing approval data
    if (consentIssuingApprovalAudits.isEmpty()) {
      var consentIssuingApprovalOptional = consentIssuingApprovalService.findConsentIssuingApproval(application);

      if (consentIssuingApprovalOptional.isEmpty()) {
        return Collections.emptyList();
      }

      caseEvents.add(
          getConsentIssuingApprovalEvent(
              consentIssuingApprovalOptional.get(),
              applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
      );
      return caseEvents;
    }

    var applicationVersion = applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId());

    // get case events for consent issuing approvals audit data
    for (var consentIssuingApprovalAudit : consentIssuingApprovalAudits) {

      if (consentIssuingApprovalAudit.revisionType().equals(RevisionType.ADD)) {
        caseEvents.add(
            getConsentIssuingApprovalEventFromAudit(consentIssuingApprovalAudit, APPROVED_FOR_ISSUE, applicationVersion)
        );
      }

      if (consentIssuingApprovalAudit.revisionType().equals(RevisionType.DEL)) {
        caseEvents.add(
            getConsentIssuingApprovalEventFromAudit(consentIssuingApprovalAudit, UNAPPROVED_FOR_ISSUE, applicationVersion)
        );
      }
    }

    return caseEvents;
  }

  private CaseEvent getConsentIssuingApprovalEvent(ConsentIssuingApproval consentIssuingApproval,
                                                   ApplicationVersion applicationVersion) {
    return CaseEvent.builder(applicationVersion)
        .withEventType(CaseEventType.APPROVED_FOR_ISSUE)
        .withMainEventUserWuaId(consentIssuingApproval.getApprovedByWuaId())
        .withEventDateTime(consentIssuingApproval.getApprovedInstant())
        .build();
  }

  private CaseEvent getConsentIssuingApprovalEventFromAudit(FieldConsentsAudit<ConsentIssuingApproval> fieldConsentsAudit,
                                                            CaseEventType eventType,
                                                            ApplicationVersion applicationVersion) {
    return CaseEvent.newBuilderForAuditRevision(
        fieldConsentsAudit.auditRevision(),
        applicationVersion)
        .withEventType(eventType)
        .build();
  }
}
