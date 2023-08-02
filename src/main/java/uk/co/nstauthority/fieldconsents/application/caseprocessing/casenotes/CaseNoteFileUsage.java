package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;

public record CaseNoteFileUsage(
    String usageId,
    String usageType,
    String documentType
) implements FieldConsentsFileUsage {

  private static final String USAGE_TYPE = "CaseNote";

  public static CaseNoteFileUsage fromCaseNote(CaseNote caseNote) {
    return new CaseNoteFileUsage(
        caseNote.getId().toString(),
        USAGE_TYPE,
        "case-note-document"
    );
  }

}
