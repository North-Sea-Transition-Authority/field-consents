package uk.co.nstauthority.fieldconsents.startapplication;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class StartApplicationFormValidatorTest {

  private Errors errors;

  private StartApplicationFormValidator formValidator;

  private StartApplicationForm form;

  @BeforeEach
  void setUp() {
    formValidator = new StartApplicationFormValidator();
    form = new StartApplicationForm(ApplicationType.PRODUCTION);
  }

  @Test
  void validate_whenValidForm() {
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenInvalidForm() {
    form.setApplicationType(null);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    Map<String, List<String>> errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("applicationType", Collections.singletonList("Select the application type"))
    );
  }
}