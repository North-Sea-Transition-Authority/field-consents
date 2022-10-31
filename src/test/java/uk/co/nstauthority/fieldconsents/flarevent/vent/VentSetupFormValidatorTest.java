package uk.co.nstauthority.fieldconsents.flarevent.vent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class VentSetupFormValidatorTest {

  private VentSetupFormValidator validator;

  private Errors errors;

  private VentSetupForm ventSetupForm;

  @BeforeEach
  void setUp() {
    validator = new VentSetupFormValidator();
    ventSetupForm = new VentSetupForm();
    errors = new BeanPropertyBindingResult(ventSetupForm, "form");
  }

  @Test
  void validate_emptyForm() {

    ValidationUtils.invokeValidator(validator, ventSetupForm, errors);

    Map<String, List<String>> errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap)
        .containsExactly(
            entry("hasOtherVentsToAdd",
                Collections.singletonList(VentSetupFormValidator.HAS_OTHER_VENTS_TO_ADD_EMPTY))
        );
  }

  @Test
  void validate_validFormHasOtherVentsToAdd() {
    ventSetupForm.setHasOtherVentsToAdd(Boolean.TRUE);

    ValidationUtils.invokeValidator(validator, ventSetupForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_validFormNoOtherVentsToAdd() {
    ventSetupForm.setHasOtherVentsToAdd(Boolean.FALSE);

    ValidationUtils.invokeValidator(validator, ventSetupForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
