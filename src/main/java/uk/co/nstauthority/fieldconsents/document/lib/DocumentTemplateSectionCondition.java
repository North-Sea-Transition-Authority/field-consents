package uk.co.nstauthority.fieldconsents.document.lib;

public interface DocumentTemplateSectionCondition {

  String getMnemonic();

  String getTitle();

  boolean isApplicable(DocumentTemplateDto documentTemplateDto);

  boolean evaluate(DocumentInstanceDto documentInstanceDto);
}
