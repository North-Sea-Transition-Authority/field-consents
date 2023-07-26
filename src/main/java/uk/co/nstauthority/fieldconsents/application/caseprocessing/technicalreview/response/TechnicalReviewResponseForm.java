package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response;

import java.util.ArrayList;
import java.util.List;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;

public record TechnicalReviewResponseForm(
    TechnicalReviewResponseType responseType,
    StringInput consentConditions,
    StringInput rejectionReason,
    List<UploadedFileForm> documents
) {

  public TechnicalReviewResponseForm {
    consentConditions = new StringInput("consentConditions", "consent conditions");
    rejectionReason = new StringInput("rejectionReason", "rejection reason");
    documents = new ArrayList<>();
  }

  public static TechnicalReviewResponseForm empty() {
    return new TechnicalReviewResponseForm(null, null, null, null);
  }

}
