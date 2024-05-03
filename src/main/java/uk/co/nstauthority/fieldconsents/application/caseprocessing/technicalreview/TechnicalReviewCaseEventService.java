package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.hibernate.envers.RevisionType.ADD;
import static org.hibernate.envers.RevisionType.MOD;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType.TECHNICAL_REVIEW_REASSIGNED;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
public class TechnicalReviewCaseEventService implements CaseEventService<Application> {

  private final FieldConsentsAuditService auditService;
  private final TechnicalReviewService technicalReviewService;

  public TechnicalReviewCaseEventService(FieldConsentsAuditService auditService,
                                         TechnicalReviewService technicalReviewService) {
    this.auditService = auditService;
    this.technicalReviewService = technicalReviewService;
  }

  @Override
  public Set<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new HashSet<CaseEvent>();
    var technicalReviews = technicalReviewService.getTechnicalReviewsByApplication(application);

    // get case events for technical reviews audit data - technical review reassigned
    var technicalReviewsById = technicalReviews
        .stream()
        .collect(Collectors.toMap(TechnicalReview::getId, Function.identity()));

    var auditsByTechnicalReviewId = auditService.getAuditsFor(
            TechnicalReview.class,
            TechnicalReview::getId,
            technicalReviewsById.values())
        .stream()
        .filter(technicalReviewAudit ->
            technicalReviewAudit.revisionType().equals(ADD)
            || technicalReviewAudit.revisionType().equals(MOD))
        .collect(Collectors.groupingBy(
            audit -> audit.entity().getId(),
            LinkedHashMap::new,
            Collectors.toList()
        ));

    // get case events for technical reviews data - technical review requested/completed
    for (var technicalReview : technicalReviews) {
      caseEvents.add(
          getTechnicalReviewRequestedEventWithAudits(technicalReview, auditsByTechnicalReviewId.get(technicalReview.getId()))
      );

      if (TechnicalReviewStatus.CLOSED.equals(technicalReview.getTechnicalReviewStatus())) {
        caseEvents.add(
            getTechnicalReviewCompletedEvent(technicalReview)
        );
      }
    }

    if (auditsByTechnicalReviewId.isEmpty()) {
      return caseEvents;
    }

    for (var audits : auditsByTechnicalReviewId.values()) {
      for (var i = 0; i < audits.size(); i++) {
        var previousTechnicalReviewAuditEntry = i == 0 ? null : audits.get(i - 1);
        var currentTechnicalReviewAuditEntry = audits.get(i);

        var technicalReviewId = currentTechnicalReviewAuditEntry.entity().getId();
        var requestApplicationVersion = technicalReviewsById.get(technicalReviewId).getRequestApplicationVersion();

        if (previousTechnicalReviewAuditEntry != null) {
          getTechnicalReviewReassignedEvent(
              requestApplicationVersion,
              previousTechnicalReviewAuditEntry,
              currentTechnicalReviewAuditEntry
          ).ifPresent(caseEvents::add);
        }
      }
    }

    return caseEvents;
  }

  private CaseEvent getTechnicalReviewRequestedEventWithAudits(TechnicalReview technicalReview,
                                                               List<FieldConsentsAudit<TechnicalReview>> fieldConsentsAudits) {
    // Migrated cases will have no audit rows, so we fall back to the base technical reviews data
    if (fieldConsentsAudits == null) {
      return getTechnicalReviewRequestedEvent(technicalReview, technicalReview.getTechnicalReviewerWuaId());
    }

    var initialTechnicalReviewerWuaId = fieldConsentsAudits.stream()
        .filter(audit -> RevisionType.ADD.equals(audit.revisionType()))
        .min(Comparator.comparing(audit -> audit.auditRevision().getCreatedDateTime()))
        .map(audit -> audit.entity().getTechnicalReviewerWuaId())
        .orElse(null);

    var technicalReviewerWuaId = initialTechnicalReviewerWuaId == null
        ? technicalReview.getTechnicalReviewerWuaId()
        : initialTechnicalReviewerWuaId;

    return getTechnicalReviewRequestedEvent(technicalReview, technicalReviewerWuaId);
  }

  private CaseEvent getTechnicalReviewRequestedEvent(TechnicalReview technicalReview, Long technicalReviewerWuaId) {
    return CaseEvent.builder(technicalReview.getRequestApplicationVersion())
        .withEventType(CaseEventType.TECHNICAL_REVIEW_REQUESTED)
        .withMainEventUserWuaId(technicalReview.getRequestedByWuaId())
        .withEventDateTime(technicalReview.getRequestedDateTime())
        .withEventText(getTechnicalReviewRequestText(technicalReview))
        .withOtherEventUserWuaId(technicalReviewerWuaId)
        .build();
  }

  private CaseEvent getTechnicalReviewCompletedEvent(TechnicalReview technicalReview) {
    return CaseEvent.builder(technicalReview.getResponseApplicationVersion())
        .withEventType(CaseEventType.TECHNICAL_REVIEW_COMPLETED)
        .withMainEventUserWuaId(technicalReview.getRespondedByWuaId())
        .withEventDateTime(technicalReview.getRespondedDateTime())
        .withEventText(getTechnicalReviewResponseText(technicalReview))
        .build();
  }

  Optional<CaseEvent> getTechnicalReviewReassignedEvent(ApplicationVersion requestApplicationVersion,
                                                        FieldConsentsAudit<TechnicalReview> previousTechnicalReviewAuditEntry,
                                                        FieldConsentsAudit<TechnicalReview> currentTechnicalReviewAuditEntry) {
    var previousTechnicalReviewerWuaId = previousTechnicalReviewAuditEntry.entity().getTechnicalReviewerWuaId();

    var currentTechnicalReviewerWuaId = currentTechnicalReviewAuditEntry.entity().getTechnicalReviewerWuaId();
    if (previousTechnicalReviewerWuaId.equals(currentTechnicalReviewerWuaId)
        || !MOD.equals(currentTechnicalReviewAuditEntry.revisionType())) {
      return Optional.empty();
    }

    var caseEvent = CaseEvent.newBuilderForAuditRevision(
            currentTechnicalReviewAuditEntry.auditRevision(),
            requestApplicationVersion)
        .withEventType(TECHNICAL_REVIEW_REASSIGNED)
        .withOtherEventUserWuaId(currentTechnicalReviewerWuaId)
        .build();

    return Optional.of(caseEvent);
  }

  private String getTechnicalReviewRequestText(TechnicalReview technicalReview) {
    // null check to cope with migrated data
    var deadlineText = Optional.ofNullable(technicalReview.getDeadlineDateTime())
        .map(deadlineDateTime -> DateUtils.format(deadlineDateTime, DateUtils.DATE_TIME))
        .map("Deadline: %s."::formatted)
        .orElse("");

    var requestText = Optional.ofNullable(technicalReview.getRequestText())
        .map("Notes for the reviewer: %s"::formatted)
        .orElse("");

    return "%s %s".formatted(deadlineText, requestText).strip();
  }

  private String getTechnicalReviewResponseText(TechnicalReview technicalReview) {
    // null check to cope with migrated data
    var responseTypeText = Optional.ofNullable(technicalReview.getResponseType())
        .map(responseType -> "Decision: %s.".formatted(responseType.getDisplayName()))
        .orElse("");

    // null check to cope with migrated data
    var responseTextPrompt = Optional.ofNullable(technicalReview.getResponseType())
        .map(TechnicalReviewResponseType::getResponseTextLabel)
        .orElse("Response notes");

    var responseText = Optional.ofNullable(technicalReview.getResponseText())
        .map(text -> "%s: %s".formatted(responseTextPrompt, text))
        .orElse("");

    return "%s %s".formatted(responseTypeText, responseText).strip();
  }
}
