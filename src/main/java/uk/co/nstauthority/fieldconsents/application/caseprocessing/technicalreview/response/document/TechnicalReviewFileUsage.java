package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.document;

import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;

public record TechnicalReviewFileUsage(
    String usageId,
    String usageType,
    String documentType
) implements FieldConsentsFileUsage {

  private static final String USAGE_TYPE = "TechnicalReview";

  public static TechnicalReviewFileUsage responseFrom(TechnicalReview technicalReview) {
    return new TechnicalReviewFileUsage(
        technicalReview.getId().toString(),
        USAGE_TYPE,
        "response-document"
    );
  }

}
