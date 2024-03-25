package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;

public record ConsentFileUsage(
    String usageId,
    String usageType,
    String documentType
) implements FieldConsentsFileUsage {

  static final String USAGE_TYPE = "ApplicationConsent";

  public static ConsentFileUsage generatedConsentDocumentFrom(Consent consent) {
    return new ConsentFileUsage(
        consent.getId().toString(),
        USAGE_TYPE,
        "generated-consent-document"
    );
  }

  public static ConsentFileUsage supportingConsentDocumentFrom(Consent consent) {
    return new ConsentFileUsage(
        consent.getId().toString(),
        USAGE_TYPE,
        "supporting-consent-document"
    );
  }
}
