package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

public enum ProductionConsentCheckResult {

  CONSENT_DETAILS_DO_NOT_EXIST(null),
  WITHIN_ACTIVE_CONSENT(null),
  NOT_WITHIN_ACTIVE_CONSENT("An active production consent does not exist for all fields for the duration of this application."),
  ;

  private final String warning;

  ProductionConsentCheckResult(String warning) {
    this.warning = warning;
  }

  public String getWarning() {
    return warning;
  }

}
