package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEvent;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.caseevents.CaseEventType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class TechnicalReviewCaseEventService implements CaseEventService<Application> {

  private final TechnicalReviewService technicalReviewService;

  public TechnicalReviewCaseEventService(TechnicalReviewService technicalReviewService) {
    this.technicalReviewService = technicalReviewService;
  }

  @Override
  public List<CaseEvent> getCaseEvents(Application application) {
    var caseEvents = new ArrayList<CaseEvent>();
    var technicalReviews = technicalReviewService.getTechnicalReviewsByApplication(application);

    for (TechnicalReview technicalReview : technicalReviews) {
      caseEvents.add(
          getTechnicalReviewRequestedEvent(technicalReview)
      );

      if (TechnicalReviewStatus.CLOSED.equals(technicalReview.getTechnicalReviewStatus())) {
        caseEvents.add(
            getTechnicalReviewCompletedEvent(technicalReview)
        );
      }
    }

    return caseEvents;
  }

  private CaseEvent getTechnicalReviewRequestedEvent(TechnicalReview technicalReview) {
    return CaseEvent.builder(technicalReview.getRequestApplicationVersion())
        .withEventType(CaseEventType.TECHNICAL_REVIEW_REQUESTED)
        .withMainEventUserWuaId(technicalReview.getRequestedByWuaId())
        .withEventDateTime(technicalReview.getRequestedDateTime())
        .withEventText(getTechnicalReviewRequestText(technicalReview))
        .withOtherEventUserWuaId(technicalReview.getTechnicalReviewerWuaId())
        .build();
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

  private CaseEvent getTechnicalReviewCompletedEvent(TechnicalReview technicalReview) {
    return CaseEvent.builder(technicalReview.getResponseApplicationVersion())
        .withEventType(CaseEventType.TECHNICAL_REVIEW_COMPLETED)
        .withMainEventUserWuaId(technicalReview.getRespondedByWuaId())
        .withEventDateTime(technicalReview.getRespondedDateTime())
        .withEventText(getTechnicalReviewResponseText(technicalReview))
        .build();
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
