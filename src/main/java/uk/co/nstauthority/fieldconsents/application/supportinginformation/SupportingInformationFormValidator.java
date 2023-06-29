package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.validation.FileValidationUtil;

@Service
public class SupportingInformationFormValidator implements Validator {

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return SupportingInformationForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (SupportingInformationForm) target;
    var applicationType = form.getApplicationVersion().getApplication().getType();

    StringInputValidator.builder()
        .validate(form.getNotes(), errors);

    if (ApplicationTypeFeature.ERAP_SUPPORTING_INFORMATION.allowed(applicationType)) {
      StringInputValidator.builder()
          .validate(form.getErapNotes(), errors);
    }

    FileValidationUtil.validateFilesHaveDescriptions(
        form.getSupportingDocuments(),
        "supportingDocuments",
        errors
    );
  }
}
