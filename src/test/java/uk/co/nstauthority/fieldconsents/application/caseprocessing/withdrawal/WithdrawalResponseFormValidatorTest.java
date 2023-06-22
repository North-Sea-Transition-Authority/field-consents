package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal.WithdrawalResponseFormValidator.WITHDRAWAL_RESPONSE_EMPTY;

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
class WithdrawalResponseFormValidatorTest {

  private Errors errors;

  private WithdrawalResponseForm form;

  private WithdrawalResponseFormValidator formValidator;


  @BeforeEach
  void setUp() {
    formValidator = new WithdrawalResponseFormValidator();
    form = new WithdrawalResponseForm();
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(WithdrawalResponseForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidFormAndAccepted_thenNoErrors() {
    form.setResponseStatus(WithdrawalStatus.ACCEPTED);

    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenValidFormAndRejected_thenNoErrors() {
    form.setResponseStatus(WithdrawalStatus.REJECTED);
    form.getResponseText().setInputValue("test");

    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm_noResponse_thenError() {
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("responseStatus", Collections.singletonList(WITHDRAWAL_RESPONSE_EMPTY))
    );
  }

  @Test
  void validate_whenInvalidForm_rejectedWithNoReason_thenError() {
    form.setResponseStatus(WithdrawalStatus.REJECTED);

    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("responseText.inputValue", Collections.singletonList("Enter the reason for rejecting the withdrawal request"))
    );
  }
}
