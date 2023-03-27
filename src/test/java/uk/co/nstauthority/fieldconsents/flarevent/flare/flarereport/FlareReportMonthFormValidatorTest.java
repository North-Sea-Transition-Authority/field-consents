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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
    flareReportMonthForm.setComments(ValidatorTestingUtil.STRING_300_CHARACTERS);
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
                Collections.singletonList("Enter days of total shutdown"))
        );
  }


  @ParameterizedTest
  @CsvSource({
      "31, Days of total shutdown must be 30 or fewer",
      "-1, Days of total shutdown must be 0 or more",
      "x, Days of total shutdown must be a whole number"
  })
  void validate_shutDownDays_invalid(String shutDownDays, String errorMessage) {
    flareReportMonthForm = getStubFlareReportMonthForm();
    flareReportMonthForm.getShutDownDays().setInputValue(shutDownDays);
    errors = new BeanPropertyBindingResult(flareReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("shutDownDays.inputValue", Collections.singletonList(errorMessage))
        );
  }

  @Test
  void validate_validFrom() {
    flareReportMonthForm = getStubFlareReportMonthForm();
    flareReportMonthForm.getShutDownDays().setInputValue("30");
    errors = new BeanPropertyBindingResult(flareReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportMonthForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
