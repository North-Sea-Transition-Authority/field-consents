package uk.co.nstauthority.fieldconsents.document.lib;

public record DocumentMailMergeFieldView(String mnemonic, String description) {

  static DocumentMailMergeFieldView from(DocumentMailMergeField documentMailMergeField) {
    return new DocumentMailMergeFieldView(
        documentMailMergeField.getMnemonic(),
        documentMailMergeField.getDescription()
    );
  }
}
