package uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

@ExtendWith(MockitoExtension.class)
class ProjectPurposeFormValidatorTest {

  @InjectMocks
  private ProjectPurposeFormValidator validator;

  @Test
  void validate_emptyRadioOnSubmission() {
    var form = ProjectPurposeForm.empty();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1)
        .first()
        .extracting(
            FieldError::getField,
            FieldError::getDefaultMessage
        ).containsExactly(
            "forPurposeOfEiaRegs",
            "Select yes if this \"project\" is for the purposes of aforementioned regulations"
        );
  }

  @Test
  void validate_forPurposeOfEiaRegsTrue_withRationale() {
    var form = new ProjectPurposeForm(true, null, null);
    form.setRationaleForPurposeOfEiaRegs("a rationale");
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_forPurposeOfEiaRegsTrue_withoutRationale_fails() {
    var form = new ProjectPurposeForm(true, null, null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1)
        .first()
        .extracting(
            FieldError::getField,
            FieldError::getDefaultMessage
        ).containsExactly(
            "rationaleForPurposeOfEiaRegs.inputValue",
            "Enter why this is a \"project\""
        );
  }

  @Test
  void validate_forPurposeOfEiaRegsFalse_withRationale() {
    var form = new ProjectPurposeForm(false, null, null);
    form.setRationaleNotForPurposeOfEiaRegs("a rationale");
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_forPurposeOfEiaRegsFalse_withoutRationale_fails() {
    var form = new ProjectPurposeForm(false, null, null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1)
        .first()
        .extracting(
            FieldError::getField,
            FieldError::getDefaultMessage
        ).containsExactly(
            "rationaleNotForPurposeOfEiaRegs.inputValue",
            "Enter why this is not a \"project\""
        );
  }
}
