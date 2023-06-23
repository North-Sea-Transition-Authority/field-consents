package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;

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

    validateFilesHaveDescriptions(form.getSupportingDocuments(), errors);
  }

  private void validateFilesHaveDescriptions(List<UploadedFileForm> fileForms, Errors errors) {
    for (var i = 0; i < fileForms.size(); i++) {
      var field = "supportingDocuments[%s].uploadedFileDescription".formatted(i);
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, "mandatory", "Enter a file description");
    }
  }

}
