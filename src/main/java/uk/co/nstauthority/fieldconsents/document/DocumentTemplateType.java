package uk.co.nstauthority.fieldconsents.document;

public enum DocumentTemplateType {
  PRODUCTION_CONSENT("Production Consent document for this application"),
  FLARE_CONSENT("Vent Consent document for this application"),
  VENT_CONSENT("Flare Consent document for this application"),
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

}
