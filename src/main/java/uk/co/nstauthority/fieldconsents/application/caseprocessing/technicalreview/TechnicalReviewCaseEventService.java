package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    var technicalReviewDeadlineText = String.format("Deadline date time %s.",
        DateUtils.format(technicalReview.getDeadlineDateTime(), DateUtils.DATE_TIME)
    );

    if (Objects.nonNull(technicalReview.getRequestText())) {
      var technicalReviewRequestText = String.format(" Notes for the reviewer: %s",
          technicalReview.getRequestText()
      );
      return technicalReviewDeadlineText.concat(technicalReviewRequestText);
    }
    return technicalReviewDeadlineText;
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
    var technicalReviewDecisionText = "Decision: " + technicalReview.getResponseType().getDisplayName();

    var technicalReviewResponseText =
        Objects.nonNull(technicalReview.getResponseText())
            ? ". %s: %s".formatted(technicalReview.getResponseType().getResponseTextLabel(), technicalReview.getResponseText())
            : "";

    return technicalReviewDecisionText + technicalReviewResponseText;
  }
}
