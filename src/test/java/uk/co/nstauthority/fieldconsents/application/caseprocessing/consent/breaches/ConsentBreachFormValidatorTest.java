package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

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
class ConsentBreachFormValidatorTest {

  private Errors errors;

  private ConsentBreachForm form;

  private ConsentBreachFormValidator formValidator;


  @BeforeEach
  void setUp() {
    form = new ConsentBreachForm();
    formValidator = new ConsentBreachFormValidator();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(ConsentBreachForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    form.setConsentBreachText("test");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm_emptyCaseNote_thenError() {
    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("consentBreachText.inputValue", Collections.singletonList("Enter details of the breach"))
    );
  }
}
