package uk.co.nstauthority.fieldconsents.application.consentlength;

import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public record ConsentLengthView(
    ConsentLengthType consentLengthType,
    String formattedShortTermStartDate,
    Integer annualConsentYear,
    Integer longTermStartYear
) {

  public static ConsentLengthView from(ConsentLengthDetails consentLengthDetails) {
    return new ConsentLengthView(
        consentLengthDetails.getConsentLength(),
        DateUtils.format(consentLengthDetails.getShortTermStartDate(), DateUtils.SHORT_DATE),
        consentLengthDetails.getAnnualConsentYear(),
        consentLengthDetails.getLongTermStartYear()
    );
  }
}
