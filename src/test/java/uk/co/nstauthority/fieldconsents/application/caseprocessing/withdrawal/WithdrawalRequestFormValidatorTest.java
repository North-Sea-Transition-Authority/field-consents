package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

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
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class WithdrawalRequestFormValidatorTest {

  private Errors errors;

  private WithdrawalRequestForm form;

  private WithdrawalRequestFormValidator formValidator;


  @BeforeEach
  void setUp() {
    formValidator = new WithdrawalRequestFormValidator();
    form = new WithdrawalRequestForm();
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(WithdrawalRequestForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    form.setRequestText("test");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm_emptyRequestText_thenError() {
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("requestText.inputValue", Collections.singletonList("Enter reason for withdrawal"))
    );
  }
}
