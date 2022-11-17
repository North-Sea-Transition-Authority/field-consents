package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class FlareReportPeriodFormValidatorTest {

  private FlareReportPeriodForm flareReportPeriodForm;

  private FlareReportPeriodFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    validator = new FlareReportPeriodFormValidator();
  }

  @Test
  void validate_emptyForm() {
    flareReportPeriodForm = new FlareReportPeriodForm();

    errors = new BeanPropertyBindingResult(flareReportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportPeriodForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("hasDataForPeriod",
                Collections.singletonList(FlareReportPeriodFormValidator.HAS_DATA_FOR_PERIOD_EMPTY))
        );

  }

  @Test
  void validate_hasDataForPeriodTrue() {
    flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.setHasDataForPeriod(Boolean.TRUE);

    errors = new BeanPropertyBindingResult(flareReportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportPeriodForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_hasDataForPeriodFalseElseEmpty() {
    flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.setHasDataForPeriod(Boolean.FALSE);

    errors = new BeanPropertyBindingResult(flareReportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportPeriodForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("reportEndMonth.inputValue",
                Collections.singletonList("Month must have a value.")),
            entry("reportEndYear.inputValue",
                Collections.singletonList("Year must have a value."))
        );

  }

  @Test
  void validate_invalidYear() {
    flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.setHasDataForPeriod(Boolean.FALSE);
    flareReportPeriodForm.setReportEndYear("a");
    flareReportPeriodForm.setReportEndMonth(Month.JANUARY.name());

    errors = new BeanPropertyBindingResult(flareReportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportPeriodForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("reportEndYear.inputValue",
                Collections.singletonList("Year must be a whole number."))
        );
  }

  @Test
  void validate_invalidMonth() {
    flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.setHasDataForPeriod(Boolean.FALSE);
    flareReportPeriodForm.setReportEndYear(String.valueOf(Year.now()));
    flareReportPeriodForm.setReportEndMonth("NOTAMONTH");

    errors = new BeanPropertyBindingResult(flareReportPeriodForm, "form");

    assertThatThrownBy(() -> ValidationUtils.invokeValidator(validator, flareReportPeriodForm, errors))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No enum constant java.time.Month.NOTAMONTH");

  }

  @Test
  void validate_invalidInFuture() {
    flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.setHasDataForPeriod(Boolean.FALSE);
    YearMonth yearMonthInFuture = YearMonth.now().plusMonths(1);
    flareReportPeriodForm.setReportEndYear(String.valueOf(yearMonthInFuture.getYear()));
    flareReportPeriodForm.setReportEndMonth(yearMonthInFuture.getMonth().name());

    errors = new BeanPropertyBindingResult(flareReportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportPeriodForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("reportEndMonth.inputValue",
                Collections.singletonList(FlareReportPeriodFormValidator.REPORT_END_MONTH_IN_FUTURE))
        );
  }

  @Test
  void validate_validForm() {
    flareReportPeriodForm = new FlareReportPeriodForm();
    flareReportPeriodForm.setHasDataForPeriod(Boolean.FALSE);
    YearMonth currentYearMonth = YearMonth.now();
    flareReportPeriodForm.setReportEndYear(String.valueOf(currentYearMonth.getYear()));
    flareReportPeriodForm.setReportEndMonth(currentYearMonth.getMonth().name());

    errors = new BeanPropertyBindingResult(flareReportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, flareReportPeriodForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

}
