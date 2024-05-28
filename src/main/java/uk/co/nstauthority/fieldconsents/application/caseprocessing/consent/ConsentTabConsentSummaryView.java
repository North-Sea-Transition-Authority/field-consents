package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentFigureUnitView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner.ConsentFieldEquityPartnersView;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

public record ConsentTabConsentSummaryView(
    ApplicationType applicationType,
    ConsentLengthType consentLengthType,
    String issuedByUser,
    String formattedIssuedDate,
    ConsentStatus consentStatus,
    String consentSupersededByApplicationReference,
    ConsentDataView consentDataView,
    ConsentFigureUnitView consentFigureUnitView,
    @Nullable ConsentFieldEquityPartnersView consentFieldEquityPartnersView,
    List<SummaryFileView> summaryFileViews
) {

  static ConsentTabConsentSummaryView from(
      ApplicationType applicationType,
      ConsentLengthType consentLengthType,
      ServiceUserDetail issuedByUser,
      Instant issuedDateInstant,
      ConsentStatus consentStatus,
      String consentSupersededByApplicationReference,
      ConsentDataView consentDataView,
      ConsentFigureUnitView consentFigureUnitView,
      ConsentFieldEquityPartnersView consentFieldEquityPartnersView,
      List<SummaryFileView> summaryFileViews
  ) {
    return new ConsentTabConsentSummaryView(
        applicationType,
        consentLengthType,
        issuedByUser.displayNameAndEmail(),
        DateUtils.format(issuedDateInstant, DateUtils.DATE_TIME),
        consentStatus,
        consentSupersededByApplicationReference,
        consentDataView,
        consentFigureUnitView,
        consentFieldEquityPartnersView,
        summaryFileViews
    );
  }
}
