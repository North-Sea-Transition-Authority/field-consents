package uk.co.nstauthority.fieldconsents.application.caseprocessing.update.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateResponseFormValidatorTest {

  private Errors errors;

  private ApplicationUpdateResponseForm form;

  private ApplicationUpdateResponseFormValidator formValidator;
  
  @BeforeEach
  void setUp() {
    form = ApplicationUpdateResponseForm.empty();
    formValidator = new ApplicationUpdateResponseFormValidator();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(ApplicationUpdateResponseForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidForm_withRequestedChangesOnly_thenNoErrors() {
    var form = new ApplicationUpdateResponseForm(ApplicationUpdateResponseType.REQUESTED_CHANGES_ONLY, null);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenValidForm_withOtherChanges_thenNoErrors() {
    var form = new ApplicationUpdateResponseForm(ApplicationUpdateResponseType.OTHER_CHANGES, null);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("otherChangesDescription.inputValue", Collections.singletonList("Enter other changes description"))
    );
  }

  @Test
  void validate_whenResponseTypeNotSelected_thenError() {
    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("responseType", Collections.singletonList("Select an option to describe the updates made"))
    );
  }

  @Test
  void validate_whenValidForm_withOtherChangesAndNoDescription_thenError() {
    var form = new ApplicationUpdateResponseForm(
        ApplicationUpdateResponseType.OTHER_CHANGES,
        new StringInput("otherChangesDescription", "other changes description")
    );
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("otherChangesDescription.inputValue", Collections.singletonList("Enter other changes description"))
    );
  }
}
