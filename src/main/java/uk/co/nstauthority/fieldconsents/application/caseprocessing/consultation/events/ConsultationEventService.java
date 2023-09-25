package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.events;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REQUESTED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
class ConsultationEventService implements CaseEventService<Application> {

  private final ConsultationService consultationService;
  private final ConsultationAuditService consultationAuditService;

  ConsultationEventService(
      ConsultationService consultationService,
      ConsultationAuditService consultationAuditService
  ) {
    this.consultationService = consultationService;
    this.consultationAuditService = consultationAuditService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var consultationsById = consultationService.getConsultationsByApplication(application)
        .stream()
        .collect(Collectors.toMap(Consultation::getId, Function.identity()));

    var audits = consultationAuditService.getConsultationAudits(consultationsById.values());
    var caseEvents = new ArrayList<CaseEvent>();
    var modCount = 0;

    for (var consultationAudit : audits) {
      var eventType = switch (consultationAudit.revisionType()) {
        case ADD -> CONSULTATION_REQUESTED;
        case MOD -> modCount++ == 0 ? CONSULTATION_ASSIGNED : CONSULTATION_REASSIGNED; // TODO: FCS-122
        default -> null;
      };

      if (Objects.isNull(eventType)) {
        continue;
      }

      var applicationVersion = consultationsById.get(consultationAudit.consultationId()).getRequestApplicationVersion();
      var caseEventBuilder = CaseEvent.builder(applicationVersion)
          .withEventType(eventType)
          .withEventDateTime(consultationAudit.createdDateTime()) // when the audit happened
          .withMainEventUserWuaId(consultationAudit.triggeredByWuaId());

      switch (eventType) {
        case CONSULTATION_REQUESTED -> caseEventBuilder
            .withEventText(DateUtils.format(consultationAudit.requestDeadline(), DateUtils.DATE_TIME));
        case CONSULTATION_ASSIGNED, CONSULTATION_REASSIGNED -> caseEventBuilder
            .withOtherEventUserWuaId(consultationAudit.responderWuaId());
        default -> {
        }
      }

      caseEvents.add(caseEventBuilder.build());
    }

    return caseEvents;
  }

}
