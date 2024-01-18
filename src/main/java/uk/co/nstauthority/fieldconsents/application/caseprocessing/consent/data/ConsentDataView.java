package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.format;

import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record ConsentDataView(
    String consentStartDate,
    String consentEndDate
) {

  public static ConsentDataView from(ConsentData consentData) {
    return new ConsentDataView(
        format(consentData.getConsentStartDate(), DateUtils.LONG_DATE),
        format(consentData.getConsentEndDate(), DateUtils.LONG_DATE)
    );
  }

}
