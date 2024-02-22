package uk.co.nstauthority.fieldconsents.document;

public enum DocumentTemplateType {

  FIELD_PRODUCTION_CONSENT("Production Consent document for this application"),
  FIELD_FLARE_CONSENT("Flare Consent document for this application"),
  TERMINAL_FLARE_CONSENT("Flare Consent document for this application"),
  FIELD_VENT_CONSENT("Vent Consent document for this application"),
  TERMINAL_VENT_CONSENT("Vent Consent document for this application"),
  FLARE_AND_COMMISSIONING_LETTER("Flare and Commissioning letter document for this application")
  ;

  private final String documentInstanceDescription;

  DocumentTemplateType(String documentInstanceDescription) {
    this.documentInstanceDescription = documentInstanceDescription;
  }

  public String getMnemonic() {
    return name();
  }

  public String getDocumentInstanceDescription() {
    return documentInstanceDescription;
  }

  public boolean isApplicableToFieldApplications() {
    return this == FIELD_PRODUCTION_CONSENT
        || this == FIELD_FLARE_CONSENT
        || this == FIELD_VENT_CONSENT
        || this == FLARE_AND_COMMISSIONING_LETTER;
  }

  public boolean isApplicableToTerminalApplications() {
    return this == TERMINAL_FLARE_CONSENT || this == TERMINAL_VENT_CONSENT;
  }

  public boolean isConsent() {
    return this == FIELD_PRODUCTION_CONSENT
        || this == FIELD_FLARE_CONSENT
        || this == TERMINAL_FLARE_CONSENT
        || this == FIELD_VENT_CONSENT
        || this == TERMINAL_VENT_CONSENT;
  }

  public static DocumentTemplateType getByMnemonic(String mnemonic) {
    return valueOf(mnemonic);
  }
}
