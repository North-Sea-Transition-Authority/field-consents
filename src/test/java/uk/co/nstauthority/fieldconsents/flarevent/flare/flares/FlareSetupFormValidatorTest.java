package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

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

class FlareSetupFormValidatorTest {

  private FlareSetupFormValidator validator;

  private Errors errors;

  private FlareSetupForm flareSetupForm;

  @BeforeEach
  void setUp() {
    validator = new FlareSetupFormValidator();
    flareSetupForm = new FlareSetupForm();
    errors = new BeanPropertyBindingResult(flareSetupForm, "form");
  }

  @Test
  void validate_emptyForm() {

    ValidationUtils.invokeValidator(validator, flareSetupForm, errors);

    Map<String, List<String>> errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap)
        .containsExactly(
            entry("hasOtherFlaresToAdd",
                Collections.singletonList(FlareSetupFormValidator.HAS_OTHER_FLARES_TO_ADD_EMPTY))
        );
  }

  @Test
  void validate_validFormHasOtherFlaresToAdd() {
    flareSetupForm.setHasOtherFlaresToAdd(Boolean.TRUE);

    ValidationUtils.invokeValidator(validator, flareSetupForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_validFormNoOtherFlaresToAdd() {
    flareSetupForm.setHasOtherFlaresToAdd(Boolean.FALSE);

    ValidationUtils.invokeValidator(validator, flareSetupForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
