package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import static org.hibernate.envers.RevisionType.ADD;
import static org.hibernate.envers.RevisionType.MOD;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_ASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REASSIGNED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.CONSULTATION_REQUESTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.envers.RevisionType;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
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
    var consultations = consultationService.getConsultationsByApplication(application);

    // get case events for consultations audit data - consultation assigned / reassigned
    var consultationsById = consultations
        .stream()
        .collect(Collectors.toMap(Consultation::getId, Function.identity()));

    var auditsByConsultationId = auditService.getAuditsFor(
            Consultation.class,
            Consultation::getId,
            consultationsById.values())
        .stream()
        .filter(consultationFieldConsentsAudit ->
            consultationFieldConsentsAudit.revisionType().equals(ADD)
            || consultationFieldConsentsAudit.revisionType().equals(MOD))
        .collect(Collectors.groupingBy(
            audit -> audit.entity().getId(),
            LinkedHashMap::new,
            Collectors.toList()
        ));

    var caseEvents = new ArrayList<CaseEvent>();

    // get case events for consultations data - consultation requested / completed
    for (var consultation : consultations) {

      caseEvents.add(
          getConsultationRequestedEvent(consultation, auditsByConsultationId.get(consultation.getId()))
      );

      if (ConsultationStatus.CLOSED.equals(consultation.getStatus())) {
        caseEvents.add(
            getConsultationRespondedEvent(consultation)
        );
      }
    }

    if (auditsByConsultationId.isEmpty()) {
      return Collections.emptyList();
    }

    for (var audits : auditsByConsultationId.values()) {
      for (var i = 0; i < audits.size(); i++) {
        var previous = i == 0 ? null : audits.get(i - 1);

        if (previous == null) {
          continue;
        }

        var current = audits.get(i);

        var consultationId = current.entity().getId();
        var requestApplicationVersion = consultationsById.get(consultationId).getRequestApplicationVersion();
        caseEvents.addAll(getConsultationAssignmentCaseEvents(
            requestApplicationVersion,
            previous,
            current
        ));
      }
    }

    return caseEvents;
  }

  private CaseEvent getConsultationRequestedEvent(Consultation consultation,
                                                  List<FieldConsentsAudit<Consultation>> fieldConsentsAudits) {

    var consultationRequestedAudit = fieldConsentsAudits.stream()
        .filter(audit -> RevisionType.ADD.equals(audit.revisionType()))
        .min(Comparator.comparing(audit -> audit.auditRevision().getCreatedDateTime()))
        .orElseThrow();

    return CaseEvent.newBuilderForAuditRevision(
            consultationRequestedAudit.auditRevision(),
            consultation.getRequestApplicationVersion())
        .withEventType(CONSULTATION_REQUESTED)
        .withEventText(DateUtils.format(consultationRequestedAudit.entity().getRequestDeadline(), DateUtils.DATE_TIME))
        .build();
  }

  private CaseEvent getConsultationRespondedEvent(Consultation consultation) {
    return CaseEvent.builder(consultation.getResponseApplicationVersion())
        .withEventType(CaseEventType.CONSULTATION_RESPONDED)
        .withMainEventUserWuaId(consultation.getRespondedByWuaId())
        .withEventDateTime(consultation.getRespondedAtDatetime())
        .build();
  }

  List<CaseEvent> getConsultationAssignmentCaseEvents(
      ApplicationVersion requestApplicationVersion,
      FieldConsentsAudit<Consultation> previous,
      FieldConsentsAudit<Consultation> current
  ) {
    var events = new ArrayList<CaseEvent>();

    getResponderAssignedEvent(requestApplicationVersion, previous, current).ifPresent(events::add);
    getResponderReassignedEvent(requestApplicationVersion, previous, current).ifPresent(events::add);

    return events;
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
}
