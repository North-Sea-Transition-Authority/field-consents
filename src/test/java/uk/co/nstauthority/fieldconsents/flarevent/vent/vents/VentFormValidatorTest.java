package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentFormValidator.COMMENTS_TOO_LONG;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class VentFormValidatorTest {

  public static final String DESCRIPTION_EMPTY = "Enter vent description";

  public static final String DESCRIPTION_TOO_LONG = "Vent description must be 300 characters or less";

  public static final String COMMENTS_EMPTY = "Enter comments";

  private VentFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  private VentForm ventForm;

  @BeforeEach
  void setUp() {
    validator = new VentFormValidator();
  }

  @Test
  void validate_emptyForm() {
    ventForm = new VentForm();
    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("ventType",
                Collections.singletonList(VentFormValidator.VENT_TYPE_EMPTY)),
            entry("meteredFlag",
                Collections.singletonList(VentFormValidator.METERED_FLAG_EMPTY)),
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_EMPTY))
        );
  }

  @Test
  void validate_onlyVentTypeValid() {
    ventForm = new VentForm();
    ventForm.setVentType(VentType.LP_VENT);
    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("meteredFlag",
                Collections.singletonList(VentFormValidator.METERED_FLAG_EMPTY)),
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_EMPTY))
        );
  }

  @Test
  void validate_onlyMeteredFlagTrue() {
    ventForm = new VentForm();
    ventForm.setMeteredFlag(Boolean.TRUE);
    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("ventType",
                Collections.singletonList(VentFormValidator.VENT_TYPE_EMPTY)),
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_EMPTY))
        );
  }

  @Test
  void validate_onlyMeteredFlagFalse() {
    ventForm = new VentForm();
    ventForm.setMeteredFlag(Boolean.FALSE);
    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("ventType",
                Collections.singletonList(VentFormValidator.VENT_TYPE_EMPTY)),
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_EMPTY)),
            entry("commentsMeteredNo.inputValue",
                Collections.singletonList(COMMENTS_EMPTY))
        );
  }

  @Test
  void validate_descriptionInvalidTooLong() {
    ventForm = new VentForm();
    ventForm.setVentType(VentType.HP_VENT);
    ventForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    ventForm.setMeteredFlag(Boolean.TRUE);
    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_TOO_LONG))
        );
  }

  @Test
  void validate_commentsYesInvalidTooLong() {
    ventForm = new VentForm();
    ventForm.setVentType(VentType.HP_VENT);
    ventForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    ventForm.setMeteredFlag(Boolean.TRUE);
    ventForm.getCommentsMeteredYes().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    ventForm.getCommentsMeteredNo().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("commentsMeteredYes.inputValue",
                Collections.singletonList(COMMENTS_TOO_LONG))
        );
  }

  @Test
  void validate_commentsNoInvalidTooLong() {
    ventForm = new VentForm();
    ventForm.setVentType(VentType.HP_VENT);
    ventForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    ventForm.setMeteredFlag(Boolean.FALSE);
    ventForm.getCommentsMeteredYes().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    ventForm.getCommentsMeteredNo().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("commentsMeteredNo.inputValue",
                Collections.singletonList(COMMENTS_TOO_LONG))
        );
  }

  @Test
  void validate_validForm1() {
    ventForm = new VentForm();
    ventForm.setVentType(VentType.OTHER_VENT);
    ventForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    ventForm.setMeteredFlag(Boolean.FALSE);
    ventForm.getCommentsMeteredNo().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);

    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_validForm2() {
    ventForm = new VentForm();
    ventForm.setVentType(VentType.HP_VENT);
    ventForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    ventForm.setMeteredFlag(Boolean.TRUE);
    ventForm.getCommentsMeteredYes().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);

    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_validForm3() {
    ventForm = new VentForm();
    ventForm.setVentType(VentType.LP_VENT);
    ventForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    ventForm.setMeteredFlag(Boolean.TRUE);

    errors = new BeanPropertyBindingResult(ventForm, "form");

    ValidationUtils.invokeValidator(validator, ventForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
