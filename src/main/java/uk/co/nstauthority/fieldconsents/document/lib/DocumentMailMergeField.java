package uk.co.nstauthority.fieldconsents.document.lib;

public interface DocumentMailMergeField {

  String getMnemonic();

  String getDescription();

  boolean isApplicable(DocumentTemplateDto documentTemplateDto);

  String resolve(DocumentInstanceDto documentInstanceDto);
}
