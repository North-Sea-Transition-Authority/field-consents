package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static org.hibernate.envers.RevisionType.ADD;
import static org.hibernate.envers.RevisionType.MOD;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REQUESTED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_RESPONDED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAudit;
import uk.co.nstauthority.fieldconsents.audit.FieldConsentsAuditService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
class ConsultationEventService implements CaseEventService<Application> {

  private final ConsultationService consultationService;
  private final FieldConsentsAuditService auditService;

  ConsultationEventService(ConsultationService consultationService, FieldConsentsAuditService auditService) {
    this.consultationService = consultationService;
    this.auditService = auditService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var consultationsById = consultationService.getConsultationsByApplication(application)
        .stream()
        .collect(Collectors.toMap(Consultation::getId, Function.identity()));

    var auditsByConsultationId = auditService.getAuditsFor(Consultation.class, Consultation::getId, consultationsById.values())
        .stream()
        .collect(Collectors.groupingBy(
            audit -> audit.entity().getId(),
            LinkedHashMap::new,
            Collectors.toList()
        ));

    if (auditsByConsultationId.isEmpty()) {
      return Collections.emptyList();
    }

    var caseEvents = new ArrayList<CaseEvent>();

    for (var audits : auditsByConsultationId.values()) {
      for (var i = 0; i < audits.size(); i++) {
        var previous = i == 0 ? null : audits.get(i - 1);
        var current = audits.get(i);

        var consultationId = current.entity().getId();
        var requestApplicationVersion = consultationsById.get(consultationId).getRequestApplicationVersion();
        var responseApplicationVersion = consultationsById.get(consultationId).getResponseApplicationVersion();
        caseEvents.addAll(getConsultationCaseEvents(
            requestApplicationVersion,
            responseApplicationVersion,
            previous,
            current
        ));
      }
    }

    return caseEvents;
  }

  List<CaseEvent> getConsultationCaseEvents(
      ApplicationVersion requestApplicationVersion,
      ApplicationVersion responseApplicationVersion,
      FieldConsentsAudit<Consultation> previous,
      FieldConsentsAudit<Consultation> current
  ) {
    var events = new ArrayList<CaseEvent>();
    getRequestedEvent(requestApplicationVersion, current).ifPresent(events::add);

    if (Objects.isNull(previous)) {
      return events;
    }

    getResponderAssignedEvent(requestApplicationVersion, previous, current).ifPresent(events::add);
    getResponderReassignedEvent(requestApplicationVersion, previous, current).ifPresent(events::add);
    getResponseSubmittedEvent(responseApplicationVersion, previous, current).ifPresent(events::add);

    return events;
  }

  Optional<CaseEvent> getRequestedEvent(ApplicationVersion applicationVersion, FieldConsentsAudit<Consultation> current) {
    if (!ADD.equals(current.revisionType())) {
      return Optional.empty();
    }

    var currentAuditRevision = current.auditRevision();
    var caseEvent = CaseEvent.newBuilderForAuditRevision(currentAuditRevision, applicationVersion)
        .withEventType(CONSULTATION_REQUESTED)
        .withEventText(DateUtils.format(current.entity().getRequestDeadline(), DateUtils.DATE_TIME))
        .build();

    return Optional.of(caseEvent);
  }

  Optional<CaseEvent> getResponderAssignedEvent(
      ApplicationVersion applicationVersion,
      FieldConsentsAudit<Consultation> previous,
      FieldConsentsAudit<Consultation> current
  ) {
    if (Objects.nonNull(previous.entity().getResponderWuaId())) {
      return Optional.empty();
    }

    var caseEvent = CaseEvent.newBuilderForAuditRevision(current.auditRevision(), applicationVersion)
        .withEventType(CONSULTATION_ASSIGNED)
        .withOtherEventUserWuaId(current.entity().getResponderWuaId())
        .build();

    return Optional.of(caseEvent);
  }

  Optional<CaseEvent> getResponderReassignedEvent(
      ApplicationVersion applicationVersion,
      FieldConsentsAudit<Consultation> previous,
      FieldConsentsAudit<Consultation> current
  ) {
    var previousResponder = previous.entity().getResponderWuaId();
    if (Objects.isNull(previousResponder)) {
      return Optional.empty();
    }

    var currentResponder = current.entity().getResponderWuaId();
    if (previousResponder.equals(currentResponder) || !MOD.equals(current.revisionType())) {
      return Optional.empty();
    }

    var caseEvent = CaseEvent.newBuilderForAuditRevision(current.auditRevision(), applicationVersion)
        .withEventType(CONSULTATION_REASSIGNED)
        .withOtherEventUserWuaId(currentResponder)
        .build();

    return Optional.of(caseEvent);
  }

  Optional<CaseEvent> getResponseSubmittedEvent(
      ApplicationVersion applicationVersion,
      FieldConsentsAudit<Consultation> previous,
      FieldConsentsAudit<Consultation> current
  ) {
    if (Objects.isNull(applicationVersion)) {
      return Optional.empty();
    }

    var previousRespondedByWuaId = previous.entity().getRespondedByWuaId();
    var currentRespondedByWuaId = current.entity().getRespondedByWuaId();
    if (Objects.equals(previousRespondedByWuaId, currentRespondedByWuaId)) {
      return Optional.empty();
    }

    var caseEvent = CaseEvent.newBuilderForAuditRevision(current.auditRevision(), applicationVersion)
        .withEventType(CONSULTATION_RESPONDED)
        .build();

    return Optional.of(caseEvent);
  }

}
