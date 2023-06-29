package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class CaseNoteFormValidatorTest {

  private Errors errors;

  private CaseNoteForm form;
  
  private CaseNoteFormValidator formValidator;

  @BeforeEach
  void setUp() {
    form = new CaseNoteForm();
    formValidator = new CaseNoteFormValidator();
    errors = new BeanPropertyBindingResult(form, "form");
  }
  
  @Test
  void supports() {
    assertThat(formValidator.supports(CaseNoteForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    form.setCaseNoteText("test");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm_emptyCaseNote_thenError() {
    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("caseNoteText.inputValue", Collections.singletonList("Enter the case note"))
    );
  }

  @Test
  void validate_form_hasFilesWithNoDescriptions() {
    form.setCaseNoteText("Test notes");
    form.setCaseNoteDocuments(FileUploadTestUtil.documentFormsWithMissingDescription);

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(ValidatorTestingUtil.getErrorsFieldsAndMessages(errors))
        .containsExactlyEntriesOf(Map.of(
            "caseNoteDocuments[1].uploadedFileDescription", Collections.singletonList("Enter a file description")
        ));
  }
}
