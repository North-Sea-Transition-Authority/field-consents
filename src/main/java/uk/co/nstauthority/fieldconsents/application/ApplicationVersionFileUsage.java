package uk.co.nstauthority.fieldconsents.application;

import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;

public record ApplicationVersionFileUsage(
    String usageId,
    String usageType,
    String documentType
) implements FieldConsentsFileUsage {

  private static final String USAGE_TYPE = "ApplicationVersion";

  public static ApplicationVersionFileUsage supportingDocumentFrom(Integer applicationVersionId) {
    return new ApplicationVersionFileUsage(
        applicationVersionId.toString(),
        USAGE_TYPE,
        "supporting-document"
    );
  }

  public static ApplicationVersionFileUsage supportingDocumentFrom(ApplicationVersion applicationVersion) {
    return new ApplicationVersionFileUsage(
        applicationVersion.getId().toString(),
        USAGE_TYPE,
        "supporting-document"
    );
  }

}
