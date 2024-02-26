package uk.co.nstauthority.fieldconsents.file;

public record TestFileUsage(
    String usageId,
    String usageType,
    String documentType
) implements FieldConsentsFileUsage {
}
