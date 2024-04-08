package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.util.List;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

public record ConsentTabConsentSummaryView(
    String issuedByUser,
    String formattedIssuedDate,
    List<SummaryFileView> summaryFileViews
) {

  static ConsentTabConsentSummaryView from(
      Consent consent,
      ServiceUserDetail issuedByUser,
      List<SummaryFileView> summaryFileViews
  ) {
    return new ConsentTabConsentSummaryView(
        issuedByUser.displayNameAndEmail(),
        DateUtils.format(consent.getIssuedInstant(), DateUtils.DATE_TIME),
        summaryFileViews
    );
  }
}
