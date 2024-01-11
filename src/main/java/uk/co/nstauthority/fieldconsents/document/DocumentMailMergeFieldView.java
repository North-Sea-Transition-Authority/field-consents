package uk.co.nstauthority.fieldconsents.document;

import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeField;

public record DocumentMailMergeFieldView(String mnemonic, String description) {

  static DocumentMailMergeFieldView from(DocumentMailMergeField documentMailMergeField) {
    return new DocumentMailMergeFieldView(
        documentMailMergeField.getMnemonic(),
        documentMailMergeField.getDescription()
    );
  }
}
