package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

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

class VentReportMonthFormValidatorTest {

  private VentReportMonthForm ventReportMonthForm;

  private VentReportMonthFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    FlareVentRowFormValidator ventVentRowFormValidator = new FlareVentRowFormValidator();
    validator = new VentReportMonthFormValidator(ventVentRowFormValidator);
  }

  private VentReportMonthForm getStubVentReportMonthForm() {
    ventReportMonthForm = VentReportMonthForm.from(YearMonth.of(2022, Month.NOVEMBER));
    ventReportMonthForm.getCategoryA().setInputValue("1");
    ventReportMonthForm.getCategoryB().setInputValue("1");
    ventReportMonthForm.getCategoryC().setInputValue("1");
    ventReportMonthForm.setComments(ValidatorTestingUtil.STRING_300_CHARACTERS);
    return ventReportMonthForm;
  }

  @Test
  void validate_emptyForm() {
    ventReportMonthForm = getStubVentReportMonthForm();
    errors = new BeanPropertyBindingResult(ventReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, ventReportMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("shutDownDays.inputValue",
                Collections.singletonList("Enter Days of total shutdown"))
        );
  }


  @ParameterizedTest
  @CsvSource({
      "31, Days of total shutdown must be 30 or fewer",
      "-1, Days of total shutdown must be 0 or more",
      "x, Days of total shutdown must be a whole number."
  })
  void validate_shutDownDays_invalid(String shutDownDays, String errorMessage) {
    ventReportMonthForm = getStubVentReportMonthForm();
    ventReportMonthForm.getShutDownDays().setInputValue(shutDownDays);
    errors = new BeanPropertyBindingResult(ventReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, ventReportMonthForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("shutDownDays.inputValue", Collections.singletonList(errorMessage))
        );
  }

  @Test
  void validate_validFrom() {
    ventReportMonthForm = getStubVentReportMonthForm();
    ventReportMonthForm.getShutDownDays().setInputValue("30");
    errors = new BeanPropertyBindingResult(ventReportMonthForm, "form");

    ValidationUtils.invokeValidator(validator, ventReportMonthForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
