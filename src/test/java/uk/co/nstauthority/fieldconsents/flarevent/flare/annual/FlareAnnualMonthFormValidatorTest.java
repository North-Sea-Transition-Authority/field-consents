package uk.co.nstauthority.fieldconsents.flarevent.flare.annual;

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

class FlareAnnualMonthFormValidatorTest {

  private FlareAnnualMonthForm flareAnnualMonthForm;

  private FlareAnnualMonthFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    FlareVentRowFormValidator flareVentRowFormValidator = new FlareVentRowFormValidator();
    validator = new FlareAnnualMonthFormValidator(flareVentRowFormValidator);
  }

  private FlareAnnualMonthForm getStubFlareAnnualMonthForm() {
    flareAnnualMonthForm = FlareAnnualMonthForm.from(YearMonth.of(2022, Month.NOVEMBER));
    flareAnnualMonthForm.getCategoryA().setInputValue("1");
    flareAnnualMonthForm.getCategoryB().setInputValue("1");
    flareAnnualMonthForm.getCategoryC().setInputValue("1");
    return flareAnnualMonthForm;
  }

  @Test
  void validate_emptyForm() {
    flareAnnualMonthForm = getStubFlareAnnualMonthForm();
    errors = new BeanPropertyBindingResult(flareAnnualMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareAnnualMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("comments.inputValue",
                Collections.singletonList("Comments must have a value."))
        );
  }

  @Test
  void validate_commentsMoreThan300Characters() {
    flareAnnualMonthForm = getStubFlareAnnualMonthForm();
    flareAnnualMonthForm.getComments().setInputValue(ValidatorTestingUtil.STRING_301_CHARACTERS);
    errors = new BeanPropertyBindingResult(flareAnnualMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareAnnualMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("comments.inputValue",
                Collections.singletonList("Comments must be no more than 300 characters long"))
        );
  }

  @Test
  void validate_validFrom() {
    flareAnnualMonthForm = getStubFlareAnnualMonthForm();
    flareAnnualMonthForm.getComments().setInputValue(ValidatorTestingUtil.STRING_300_CHARACTERS);
    errors = new BeanPropertyBindingResult(flareAnnualMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareAnnualMonthForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
