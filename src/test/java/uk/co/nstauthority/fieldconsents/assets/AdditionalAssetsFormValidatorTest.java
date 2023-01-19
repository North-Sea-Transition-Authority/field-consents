package uk.co.nstauthority.fieldconsents.assets;

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

class AdditionalAssetsFormValidatorTest {

  private AdditionalAssetsFormValidator validator;

  private Errors errors;

  private AdditionalAssetsForm form;


  @BeforeEach
  void setUp() {
    validator = new AdditionalAssetsFormValidator();
    form = new AdditionalAssetsForm();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void validate_emptyForm() {

    ValidationUtils.invokeValidator(validator, form, errors);

    Map<String, List<String>> errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap)
        .containsExactly(
            entry("hasOtherAssetsToAdd",
                Collections.singletonList(AdditionalAssetsFormValidator.HAS_OTHER_ASSETS_TO_ADD_EMPTY))
        );
  }

  @Test
  void validate_validFormHasOtherAssetsToAdd() {
    form.setHasOtherAssetsToAdd(Boolean.TRUE);

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_validFormNoOtherAssetsToAdd() {
    form.setHasOtherAssetsToAdd(Boolean.FALSE);

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }
}