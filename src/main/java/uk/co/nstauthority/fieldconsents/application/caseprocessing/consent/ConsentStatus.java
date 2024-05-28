package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import java.time.Clock;
import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

public enum ConsentStatus {

  ISSUED("Consent issued"),
  ACTIVE("Consent active"),
  EXPIRED("Consent expired"),
  SUPERSEDED("Consent superseded");

  private final String displayName;

  ConsentStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static ConsentStatus from(LocalDate consentStartDate, LocalDate consentEndDate, boolean consentSuperseded, Clock clock) {
    if (consentStartDate.isAfter(consentEndDate)) {
      throw new IllegalArgumentException("Consent start date %s is before consent end date %s"
          .formatted(consentStartDate, consentEndDate));
    }

    if (consentSuperseded) {
      return SUPERSEDED;
    }

    var today = LocalDate.now(clock);

    if (today.isBefore(consentStartDate)) {
      return ISSUED;
    }

    if (DateUtils.isBeforeOrEqualTo(today, consentEndDate)) {
      return ACTIVE;
    }

    return EXPIRED;
  }
}
