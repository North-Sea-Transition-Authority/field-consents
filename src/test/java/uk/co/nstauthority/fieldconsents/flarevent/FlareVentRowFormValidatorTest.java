package uk.co.nstauthority.fieldconsents.flarevent;

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

class FlareVentRowFormValidatorTest {

  private FlareVentRowForm flareVentRowForm;

  private FlareVentRowFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    validator = new FlareVentRowFormValidator();
  }

  @Test
  void validate_emptyForm() {
    flareVentRowForm = new FlareVentRowForm();
    errors = new BeanPropertyBindingResult(flareVentRowForm, "form");

    ValidationUtils.invokeValidator(validator, flareVentRowForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("categoryA.inputValue",
                Collections.singletonList("Category A must have a value.")),
            entry("categoryB.inputValue",
                Collections.singletonList("Category B must have a value.")),
            entry("categoryC.inputValue",
                Collections.singletonList("Category C must have a value.")),
            entry("comments.inputValue",
                Collections.singletonList("Comments must have a value."))
        );
  }

  @Test
  void validate_completeValidForm() {
    flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();
    errors = new BeanPropertyBindingResult(flareVentRowForm, "form");

    ValidationUtils.invokeValidator(validator, flareVentRowForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_textCategories() {
    flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();
    flareVentRowForm.setCategoryA("a");
    flareVentRowForm.setCategoryB("b");
    flareVentRowForm.setCategoryC("c");
    errors = new BeanPropertyBindingResult(flareVentRowForm, "form");

    ValidationUtils.invokeValidator(validator, flareVentRowForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("categoryA.inputValue",
                Collections.singletonList("Category A must be a number with decimal places.")),
            entry("categoryB.inputValue",
                Collections.singletonList("Category B must be a number with decimal places.")),
            entry("categoryC.inputValue",
                Collections.singletonList("Category C must be a number with decimal places."))
        );
  }

  @Test
  void validate_negativeCategories() {
    flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();
    flareVentRowForm.setCategoryA("-1");
    flareVentRowForm.setCategoryB("-2");
    flareVentRowForm.setCategoryC("-3");
    errors = new BeanPropertyBindingResult(flareVentRowForm, "form");

    ValidationUtils.invokeValidator(validator, flareVentRowForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("categoryA.inputValue",
                Collections.singletonList("Category A must be at least 0")),
            entry("categoryB.inputValue",
                Collections.singletonList("Category B must be at least 0")),
            entry("categoryC.inputValue",
                Collections.singletonList("Category C must be at least 0"))
        );
  }

  @Test
  void validate_commentsMoreThan300Characters() {
    flareVentRowForm = FlareVentRowTestUtil.getValidFlareVentRowForm();
    flareVentRowForm.setComments(ValidatorTestingUtil.STRING_301_CHARACTERS);
    errors = new BeanPropertyBindingResult(flareVentRowForm, "form");

    ValidationUtils.invokeValidator(validator, flareVentRowForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("comments.inputValue",
                Collections.singletonList("Comments must be no more than 300 characters long"))
        );
  }

}
