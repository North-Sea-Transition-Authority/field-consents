package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;
import uk.co.nstauthority.fieldconsents.validation.FileValidationUtil;

@Component
class TechnicalReviewResponseFormValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return TechnicalReviewResponseForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (TechnicalReviewResponseForm) target;

    ValidationUtils.rejectIfEmpty(
        errors,
        "responseType",
        "required",
        "Select whether you approve or reject this application"
    );

    var isRejected = TechnicalReviewResponseType.REJECT.equals(form.responseType());
    if (isRejected) {
      StringInputValidator.builder().validate(form.rejectionReason(), errors);
    }

    FileValidationUtil.validateFilesHaveDescriptions(form.documents(), "documents", errors);
  }
}
