package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

public enum ProductionConsentCheckResult {

  EXISTS(null),
  CONSENT_DETAILS_DO_NOT_EXIST(null),
  DOES_NOT_EXIST("An active production consent does not exist for a period in this application's consent."),
  EXPIRES_PART_WAY("An active production consent does fully cover the period on this application."),
  ;

  private final String warning;

  ProductionConsentCheckResult(String warning) {
    this.warning = warning;
  }

  public String getWarning() {
    return warning;
  }

}
