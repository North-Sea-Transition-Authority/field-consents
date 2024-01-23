package uk.co.nstauthority.fieldconsents.document;

public enum DocumentTemplateType {

  FIELD_PRODUCTION_CONSENT("Production Consent document for this application"),
  FIELD_FLARE_CONSENT("Vent Consent document for this application"),
  FIELD_VENT_CONSENT("Flare Consent document for this application"),
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

  public static DocumentTemplateType getByMnemonic(String mnemonic) {
    return valueOf(mnemonic);
  }

  public static boolean isField(DocumentTemplateType documentTemplateType) {
    return documentTemplateType == FIELD_PRODUCTION_CONSENT
        || documentTemplateType == FIELD_FLARE_CONSENT
        || documentTemplateType == FIELD_VENT_CONSENT;
  }

  public static boolean isConsent(DocumentTemplateType documentTemplateType) {
    return documentTemplateType == FIELD_PRODUCTION_CONSENT
        || documentTemplateType == FIELD_FLARE_CONSENT
        || documentTemplateType == FIELD_VENT_CONSENT;
  }
}
