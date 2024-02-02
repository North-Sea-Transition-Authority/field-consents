package uk.co.nstauthority.fieldconsents.application;

import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;

public record ApplicationFileUsage(
    String usageId,
    String usageType,
    String documentType
) implements FieldConsentsFileUsage {

  private static final String USAGE_TYPE = "Application";

  public static ApplicationFileUsage supportingConsentDocumentFrom(Application application) {
    return new ApplicationFileUsage(
        application.getId().toString(),
        USAGE_TYPE,
        "supporting-consent-document"
    );
  }

}
