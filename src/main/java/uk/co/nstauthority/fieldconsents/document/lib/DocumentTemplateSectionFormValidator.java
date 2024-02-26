package uk.co.nstauthority.fieldconsents.document.lib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@Component
public class DocumentTemplateSectionFormValidator {

  private final DocumentTemplateSectionConditionService documentTemplateSectionConditionService;
  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  @Autowired
  DocumentTemplateSectionFormValidator(
      DocumentTemplateSectionConditionService documentTemplateSectionConditionService,
      DocumentMailMergeFieldService documentMailMergeFieldService
  ) {
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  public void validate(DocumentTemplateSectionForm form, DocumentTemplateDto documentTemplateDto, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "title", "title.required", "Enter a title");

    var conditionMnemonic = form.conditionMnemonic();
    if (conditionMnemonic != null) {
      var condition = documentTemplateSectionConditionService.getApplicableDocumentTemplateSectionCondition(
          documentTemplateDto,
          conditionMnemonic
      );
      if (condition.isEmpty()) {
        errors.rejectValue("conditionMnemonic", "conditionMnemonic.invalid", "Select a valid condition");
      }
    }

    var content = form.content();
    if (content != null) {
      var documentMailMergeValidationResult =
          documentMailMergeFieldService.validateMailMergeFields(documentTemplateDto, content);

      if (!documentMailMergeValidationResult.isValid()) {
        errors.rejectValue("content", "content.invalid", documentMailMergeValidationResult.errorMessage());
      }
    }

    if (form.numbered() == null) {
      errors.rejectValue("numbered", "numbered.required", "Select if this section should be numbered");
    }

    if (form.hasPageBreakBefore() == null) {
      errors.rejectValue(
          "hasPageBreakBefore",
          "hasPageBreakBefore.required",
          "Select if this section should start on a new page"
      );
    }
  }
}
