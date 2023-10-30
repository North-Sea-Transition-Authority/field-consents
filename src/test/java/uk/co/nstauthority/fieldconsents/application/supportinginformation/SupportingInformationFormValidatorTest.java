package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.fileupload.FileUploadTestUtil;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class SupportingInformationFormValidatorTest {

  private Errors errors;

  private SupportingInformationForm form;

  private SupportingInformationFormValidator validator;

  @BeforeEach
  void setUp() {
    form = new SupportingInformationForm();
    validator = new SupportingInformationFormValidator();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void validate_withValidFormAndNonProductionApplication(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    form.setApplicationVersion(applicationVersion);
    form.setNotes("Test notes");
    form.setErapNotes("Test ERAP notes");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_withValidFormAndProductionApplication() {
    var productionApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    form.setApplicationVersion(productionApplicationVersion);
    form.setNotes("Test notes");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = {"FLARE", "VENT"})
  void validate_withNonValidFormAndNonProductionApplication(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    form.setApplicationVersion(applicationVersion);

    ValidationUtils.invokeValidator(validator, form, errors);

    Map<String, List<String>> errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap)
        .containsOnly(
            entry("notes.inputValue", Collections.singletonList("Enter notes")),
            entry("erapNotes.inputValue", Collections.singletonList("Enter ERAP alignment studies and projects"))
        );
  }

  @Test
  void validate_withNonValidFormAndProductionApplication() {
    var productionApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    form.setApplicationVersion(productionApplicationVersion);

    ValidationUtils.invokeValidator(validator, form, errors);

    Map<String, List<String>> errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap)
        .containsExactly(
            entry("notes.inputValue", Collections.singletonList("Enter notes"))
        );
  }

  @Test
  void validate_form_hasFilesWithNoDescriptions() {
    var productionApplicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    form.setApplicationVersion(productionApplicationVersion);
    form.setNotes("Test notes");

    form.setDocuments(FileUploadTestUtil.documentFormsWithMissingDescription);

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(ValidatorTestingUtil.getErrorsFieldsAndMessages(errors))
        .containsExactlyEntriesOf(Map.of(
            "documents[1].uploadedFileDescription", Collections.singletonList("Enter a file description")
        ));
  }
}
