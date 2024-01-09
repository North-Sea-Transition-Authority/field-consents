package uk.co.nstauthority.fieldconsents.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateSectionConditionService;

@Component
class DocumentTemplateSectionFormValidator {

  private final DocumentTemplateSectionConditionService documentTemplateSectionConditionService;

  @Autowired
  DocumentTemplateSectionFormValidator(DocumentTemplateSectionConditionService documentTemplateSectionConditionService) {
    this.documentTemplateSectionConditionService = documentTemplateSectionConditionService;
  }

  void validate(DocumentTemplateSectionForm form, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "title", "title.required", "Enter a title");

    var conditionMnemonic = form.conditionMnemonic();
    if (conditionMnemonic != null) {
      var condition = documentTemplateSectionConditionService.getDocumentTemplateSectionCondition(conditionMnemonic);
      if (condition.isEmpty()) {
        errors.rejectValue("conditionMnemonic", "conditionMnemonic.invalid", "Select a valid condition");
      }
    }
  }
}
