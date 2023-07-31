package uk.co.nstauthority.fieldconsents.application.eiadirection.projectpurpose;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

@ExtendWith(MockitoExtension.class)
class ProjectPurposeFormValidatorTest {

  @InjectMocks
  private ProjectPurposeFormValidator validator;

  @Test
  void supports() {
    assertThat(validator.supports(ProjectPurposeForm.class)).isTrue();
  }

  @Test
  void validate_emptyRadioOnSubmission() {
    var form = new ProjectPurposeForm(null);
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

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void validate(boolean forPurposeOfEiaRegs) {
    var form = new ProjectPurposeForm(forPurposeOfEiaRegs);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }
}
