package uk.co.nstauthority.fieldconsents.flarevent;

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

class FlareVentReportPeriodFormValidatorTest {

  private FlareVentReportPeriodForm reportPeriodForm;

  private FlareVentReportPeriodFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    validator = new FlareVentReportPeriodFormValidator();
  }

  @Test
  void validate_emptyForm() {
    reportPeriodForm = new FlareVentReportPeriodForm();

    errors = new BeanPropertyBindingResult(reportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, reportPeriodForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("reportEndMonth.inputValue",
                Collections.singletonList("Enter Month")),
            entry("reportEndYear.inputValue",
                Collections.singletonList("Enter Year"))
        );

  }

  @Test
  void validate_invalidYear() {
    reportPeriodForm = new FlareVentReportPeriodForm();
    reportPeriodForm.setReportEndYear("a");
    reportPeriodForm.setReportEndMonth(Month.JANUARY.name());

    errors = new BeanPropertyBindingResult(reportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, reportPeriodForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("reportEndYear.inputValue",
                Collections.singletonList("Year must be a whole number."))
        );
  }

  @Test
  void validate_invalidMonth() {
    reportPeriodForm = new FlareVentReportPeriodForm();
    reportPeriodForm.setReportEndYear(String.valueOf(Year.now()));
    reportPeriodForm.setReportEndMonth("NOTAMONTH");

    errors = new BeanPropertyBindingResult(reportPeriodForm, "form");

    assertThatThrownBy(() -> ValidationUtils.invokeValidator(validator, reportPeriodForm, errors))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("No enum constant java.time.Month.NOTAMONTH");

  }

  @Test
  void validate_invalidInFuture() {
    reportPeriodForm = new FlareVentReportPeriodForm();
    YearMonth yearMonthInFuture = YearMonth.now().plusMonths(1);
    reportPeriodForm.setReportEndYear(String.valueOf(yearMonthInFuture.getYear()));
    reportPeriodForm.setReportEndMonth(yearMonthInFuture.getMonth().name());

    errors = new BeanPropertyBindingResult(reportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, reportPeriodForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("reportEndMonth.inputValue",
                Collections.singletonList(FlareVentReportPeriodFormValidator.REPORT_END_MONTH_INVALID))
        );
  }

  @Test
  void validate_invalidCurrentMonth() {
    reportPeriodForm = new FlareVentReportPeriodForm();
    YearMonth currentYearMonth = YearMonth.now();
    reportPeriodForm.setReportEndYear(String.valueOf(currentYearMonth.getYear()));
    reportPeriodForm.setReportEndMonth(currentYearMonth.getMonth().name());

    errors = new BeanPropertyBindingResult(reportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, reportPeriodForm, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap)
        .containsOnly(
            entry("reportEndMonth.inputValue",
                Collections.singletonList(FlareVentReportPeriodFormValidator.REPORT_END_MONTH_INVALID))
        );
  }

  @Test
  void validate_validForm() {
    reportPeriodForm = new FlareVentReportPeriodForm();
    YearMonth validYearMonth = YearMonth.now().minusMonths(1);
    reportPeriodForm.setReportEndYear(String.valueOf(validYearMonth.getYear()));
    reportPeriodForm.setReportEndMonth(validYearMonth.getMonth().name());

    errors = new BeanPropertyBindingResult(reportPeriodForm, "form");

    ValidationUtils.invokeValidator(validator, reportPeriodForm, errors);

    assertThat(errors.hasErrors()).isFalse();
  }
}