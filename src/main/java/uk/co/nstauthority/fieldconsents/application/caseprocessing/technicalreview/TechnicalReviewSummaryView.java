package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record TechnicalReviewSummaryView(String deadline, String note) {

  public static TechnicalReviewSummaryView from(TechnicalReview technicalReview) {
    return new TechnicalReviewSummaryView(
        DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME),
        technicalReview.getRequestText()
    );
  }

}
