package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.preparation.documents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivum.fileuploadlibrary.core.UploadedFileTestUtil;

@ExtendWith(MockitoExtension.class)
class ConsentPreparationSupportingDocumentsFormValidatorTest {

  @InjectMocks
  private ConsentPreparationSupportingDocumentsFormValidator validator;

  @Test
  void supports() {
    assertThat(validator.supports(ConsentPreparationSupportingDocumentsForm.class)).isTrue();
  }

  @Test
  void validate() {
    var uploadedFile = UploadedFileTestUtil.newBuilder().build();
    var uploadedFileForm = FileUploadLibraryUtils.asForm(uploadedFile);

    var form = new ConsentPreparationSupportingDocumentsForm(List.of(uploadedFileForm));
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_noFiles() {
    var form = new ConsentPreparationSupportingDocumentsForm(Collections.emptyList());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_fileWithoutDescription() {
    var form = new ConsentPreparationSupportingDocumentsForm(List.of(
        FileUploadLibraryUtils.asForm(UploadedFileTestUtil.newBuilder().build()),
        FileUploadLibraryUtils.asForm(UploadedFileTestUtil.newBuilder().withDescription(null).build()),
        FileUploadLibraryUtils.asForm(UploadedFileTestUtil.newBuilder().build())
    ));
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1)
        .first()
        .extracting(FieldError::getField)
        .isEqualTo("documents[1].uploadedFileDescription");
  }
}
