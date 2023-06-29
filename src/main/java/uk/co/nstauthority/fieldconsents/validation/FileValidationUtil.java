package uk.co.nstauthority.fieldconsents.validation;

import java.util.List;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

public class FileValidationUtil {

  private FileValidationUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static void validateFilesHaveDescriptions(List<UploadedFileForm> fileForms, String fieldCollectionName, Errors errors) {
    for (var i = 0; i < fileForms.size(); i++) {
      var field = "%s[%s].uploadedFileDescription".formatted(fieldCollectionName, i);
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, field, "mandatory", "Enter a file description");
    }
  }
}
