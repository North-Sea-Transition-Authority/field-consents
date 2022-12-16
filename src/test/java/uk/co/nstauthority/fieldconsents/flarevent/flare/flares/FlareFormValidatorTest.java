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

class FlareFormValidatorTest {

  public static final String DESCRIPTION_EMPTY = "Enter Description";

  public static final String DESCRIPTION_TOO_LONG = "Description must be 300 characters or less";

  public static final String COMMENTS_EMPTY = "Enter Comments";

  public static final String COMMENTS_TOO_LONG = "Comments must be 300 characters or less";

  private FlareFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  private FlareForm flareForm;

  @BeforeEach
  void setUp() {
    validator = new FlareFormValidator();
  }

  @Test
  void validate_emptyForm() {
    flareForm = new FlareForm();
    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("flareType",
                Collections.singletonList(FlareFormValidator.FLARE_TYPE_EMPTY)),
            entry("meteredFlag",
                Collections.singletonList(FlareFormValidator.METERED_FLAG_EMPTY)),
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_EMPTY))
        );
  }

  @Test
  void validate_onlyFlareTypeValid() {
    flareForm = new FlareForm();
    flareForm.setFlareType(FlareType.LP_FLARE);
    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("meteredFlag",
                Collections.singletonList(FlareFormValidator.METERED_FLAG_EMPTY)),
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_EMPTY))
        );
  }

  @Test
  void validate_onlyMeteredFlagTrue() {
    flareForm = new FlareForm();
    flareForm.setMeteredFlag(Boolean.TRUE);
    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("flareType",
                Collections.singletonList(FlareFormValidator.FLARE_TYPE_EMPTY)),
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_EMPTY))
        );
  }

  @Test
  void validate_onlyMeteredFlagFalse() {
    flareForm = new FlareForm();
    flareForm.setMeteredFlag(Boolean.FALSE);
    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("flareType",
                Collections.singletonList(FlareFormValidator.FLARE_TYPE_EMPTY)),
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_EMPTY)),
            entry("commentsMeteredNo.inputValue",
                Collections.singletonList(COMMENTS_EMPTY))
        );
  }

  @Test
  void validate_descriptionInvalidTooLong() {
    flareForm = new FlareForm();
    flareForm.setFlareType(FlareType.HP_FLARE);
    flareForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    flareForm.setMeteredFlag(Boolean.TRUE);
    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("description.inputValue",
                Collections.singletonList(DESCRIPTION_TOO_LONG))
        );
  }

  @Test
  void validate_commentsYesInvalidTooLong() {
    flareForm = new FlareForm();
    flareForm.setFlareType(FlareType.HP_FLARE);
    flareForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    flareForm.setMeteredFlag(Boolean.TRUE);
    flareForm.getCommentsMeteredYes().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    flareForm.getCommentsMeteredNo().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("commentsMeteredYes.inputValue",
                Collections.singletonList(COMMENTS_TOO_LONG))
        );
  }

  @Test
  void validate_commentsNoInvalidTooLong() {
    flareForm = new FlareForm();
    flareForm.setFlareType(FlareType.HP_FLARE);
    flareForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    flareForm.setMeteredFlag(Boolean.FALSE);
    flareForm.getCommentsMeteredYes().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    flareForm.getCommentsMeteredNo().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("commentsMeteredNo.inputValue",
                Collections.singletonList(COMMENTS_TOO_LONG))
        );
  }

  @Test
  void validate_validForm1() {
    flareForm = new FlareForm();
    flareForm.setFlareType(FlareType.LPP_FLARE);
    flareForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    flareForm.setMeteredFlag(Boolean.FALSE);
    flareForm.getCommentsMeteredNo().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);

    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_validForm2() {
    flareForm = new FlareForm();
    flareForm.setFlareType(FlareType.HP_FLARE);
    flareForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    flareForm.setMeteredFlag(Boolean.TRUE);
    flareForm.getCommentsMeteredYes().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);

    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_validForm3() {
    flareForm = new FlareForm();
    flareForm.setFlareType(FlareType.MP_FLARE);
    flareForm.getDescription().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    flareForm.setMeteredFlag(Boolean.TRUE);

    errors = new BeanPropertyBindingResult(flareForm, "form");

    ValidationUtils.invokeValidator(validator, flareForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
