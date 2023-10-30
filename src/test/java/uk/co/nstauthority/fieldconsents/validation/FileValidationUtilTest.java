package uk.co.nstauthority.fieldconsents.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationForm;
import uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil;

class FileValidationUtilTest {

  @Test
  void validateFilesHaveDescriptions_validForms() {
    var uploadedFileForms = FileUploadTestUtil.validDocumentForms;
    var supportingInformationForm = new SupportingInformationForm();
    supportingInformationForm.setDocuments(uploadedFileForms);

    var errors = new BeanPropertyBindingResult(supportingInformationForm, "form");

    FileValidationUtil.validateFilesHaveDescriptions(uploadedFileForms, "documents", errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validateFilesHaveDescriptions_noDescription() {
    var uploadedFileForms = FileUploadTestUtil.documentFormsWithMissingDescription;
    var supportingInformationForm = new SupportingInformationForm();
    supportingInformationForm.setDocuments(uploadedFileForms);

    var errors = new BeanPropertyBindingResult(supportingInformationForm, "form");

    FileValidationUtil.validateFilesHaveDescriptions(uploadedFileForms, "documents", errors);

    assertThat(errors.hasErrors()).isTrue();
    assertThat(ValidatorTestingUtil.getErrorsFieldsAndMessages(errors))
        .containsExactlyEntriesOf(Map.of(
            "documents[1].uploadedFileDescription", Collections.singletonList("Enter a file description")
        ));
  }
}
