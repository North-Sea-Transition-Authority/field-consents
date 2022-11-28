package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.Month;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.flarevent.FlareVentRowFormValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class FlareReportMonthFormValidatorTest {

  private FlareReportMonthForm flareReportMonthForm;

  private FlareReportMonthFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    FlareVentRowFormValidator flareVentRowFormValidator = new FlareVentRowFormValidator();
    validator = new FlareReportMonthFormValidator(flareVentRowFormValidator);
  }

  private FlareReportMonthForm getStubFlareReportMonthForm() {
    flareReportMonthForm = FlareReportMonthForm.from(YearMonth.of(2022, Month.NOVEMBER));
    flareReportMonthForm.getCategoryA().setInputValue("1");
    flareReportMonthForm.getCategoryB().setInputValue("1");
    flareReportMonthForm.getCategoryC().setInputValue("1");
    return flareReportMonthForm;
  }

  @Test
  void validate_emptyForm() {
    flareReportMonthForm = getStubFlareReportMonthForm();
    errors = new BeanPropertyBindingResult(flareReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("shutDownDays.inputValue",
                Collections.singletonList("Days of total shutdown must have a value.")),
            entry("comments.inputValue",
                Collections.singletonList("Comments must have a value."))
        );
  }

  @Test
  void validate_shutDownDaysMoreThanMonthDays() {
    flareReportMonthForm = getStubFlareReportMonthForm();
    flareReportMonthForm.getComments().setInputValue("a");
    flareReportMonthForm.getShutDownDays().setInputValue("31");
    errors = new BeanPropertyBindingResult(flareReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("shutDownDays.inputValue",
                Collections.singletonList("Days of total shutdown must be less than or equal to 30"))
        );
  }

  @Test
  void validate_shutDownDaysLessThanZero() {
    flareReportMonthForm = getStubFlareReportMonthForm();
    flareReportMonthForm.getComments().setInputValue("a");
    flareReportMonthForm.getShutDownDays().setInputValue("-1");
    errors = new BeanPropertyBindingResult(flareReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("shutDownDays.inputValue",
                Collections.singletonList("Days of total shutdown must be greater than or equal to 0"))
        );
  }

  @Test
  void validate_shutDownDaysInvalidInteger() {
    flareReportMonthForm = getStubFlareReportMonthForm();
    flareReportMonthForm.getComments().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    flareReportMonthForm.getShutDownDays().setInputValue("x");
    errors = new BeanPropertyBindingResult(flareReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("shutDownDays.inputValue",
                Collections.singletonList("Days of total shutdown must be a whole number."))
        );
  }

  @Test
  void validate_commentsMoreThan300Characters() {
    flareReportMonthForm = getStubFlareReportMonthForm();
    flareReportMonthForm.getComments().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    flareReportMonthForm.getShutDownDays().setInputValue("0");
    errors = new BeanPropertyBindingResult(flareReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("comments.inputValue",
                Collections.singletonList("Comments must be no more than 300 characters long"))
        );
  }

  @Test
  void validate_validFrom() {
    flareReportMonthForm = getStubFlareReportMonthForm();
    flareReportMonthForm.getComments().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    flareReportMonthForm.getShutDownDays().setInputValue("30");
    errors = new BeanPropertyBindingResult(flareReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportMonthForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
