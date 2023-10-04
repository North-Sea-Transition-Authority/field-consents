package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation;

import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;

public record ConsultationFileUsage(
    String usageId,
    String usageType,
    String documentType
) implements FieldConsentsFileUsage {

  private static final String USAGE_TYPE = "Consultation";

  public static ConsultationFileUsage responseUsageFrom(Consultation consultation) {
    return new ConsultationFileUsage(
        consultation.getId().toString(),
        USAGE_TYPE,
        "response-document"
    );
  }

}
