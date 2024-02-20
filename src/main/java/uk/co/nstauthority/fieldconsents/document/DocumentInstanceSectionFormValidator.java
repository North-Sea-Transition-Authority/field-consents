package uk.co.nstauthority.fieldconsents.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceDto;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentMailMergeFieldService;

@Component
class DocumentInstanceSectionFormValidator {

  private final DocumentMailMergeFieldService documentMailMergeFieldService;

  @Autowired
  DocumentInstanceSectionFormValidator(DocumentMailMergeFieldService documentMailMergeFieldService) {
    this.documentMailMergeFieldService = documentMailMergeFieldService;
  }

  void validate(DocumentInstanceSectionForm form, DocumentInstanceDto documentInstanceDto, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, "title", "title.required", "Enter a title");

    var content = form.content();
    if (content != null) {
      var documentMailMergeValidationResult = documentMailMergeFieldService.validateMailMergeFields(
          documentInstanceDto.documentTemplateDto(),
          content
      );

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
