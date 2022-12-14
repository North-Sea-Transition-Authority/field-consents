package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

class ConsentLengthFormValidatorTest {

  private static final String CONSENT_LENGTH_PERIOD_EMPTY = "Select the period of the consent you are applying for";

  private static final String ANNUAL_CONSENT_YEAR_EMPTY = "Year must have a value.";

  private static final String ANNUAL_CONSENT_YEAR_INVALID = "Year must be a whole number.";

  private static final String ANNUAL_CONSENT_YEAR_GT_EQUAL = "Year must be greater than or equal to %s";

  private static final String SHORT_TERM_START_DATE_AFTER = "Start date must be after %s.";

  private static final String SHORT_TERM_START_DATE_INCOMPLETE = "Enter a complete Start date.";

  private static final String SHORT_TERM_START_DATE_INVALID = "Start date must be a valid date.";

  private static final String SHORT_TERM_START_DATE_BEFORE = "Start date must be before %s.";

  private static final String SHORT_TERM_END_DATE_AFTER = "End date must be after %s.";

  private static final String SHORT_TERM_END_DATE_INCOMPLETE = "Enter a complete End date.";

  private static final String SHORT_TERM_END_DATE_INVALID = "End date must be a valid date.";

  private static final String SHORT_TERM_END_DATE_BEFORE = "End date must be before %s.";

  private static final String LONG_TERM_START_YEAR_EMPTY = "Start year must have a value.";

  private static final String LONG_TERM_START_YEAR_INVALID = "Start year must be a whole number.";

  private static final String LONG_TERM_START_YEAR_GT_EQUAL = "Start year must be greater than or equal to %s";

  private static final String LONG_TERM_END_YEAR_EMPTY = "End year must have a value.";

  private static final String LONG_TERM_END_YEAR_INVALID = "End year must be a whole number.";

  private static final String LONG_TERM_END_YEAR_GT_EQUAL = "End year must be greater than or equal to %s";

  private static final String LONG_TERM_END_YEAR_LT_EQUAL = "End year must be less than or equal to %s";

  private ConsentLengthFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  @BeforeEach
  void setUp() {
    validator = new ConsentLengthFormValidator();
  }

  @Test
  void supports() {
    Assertions.assertThat(validator.supports(ConsentLengthForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    Assertions.assertThat(validator.supports(String.class)).isFalse();
  }


  @Test
  void validate_shortTerm_validForm() {
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(LocalDate.now(), LocalDate.now());
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_shortTerm_validForm_termJustLessThanOneYear() {
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(LocalDate.now(),
            LocalDate.now().plusYears(1).minusDays(2));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_shortTerm_termNotLessThanOneYear() {
    LocalDate startDate = LocalDate.now().plusDays(10);
    // if the start date was 1st Jan 2022 here the end date would be 31st Dec 2022
    LocalDate endDate = startDate.plusYears(1).minusDays(1);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(startDate, endDate);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDay.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_BEFORE
            .formatted(DateUtils.format(endDate, DateUtils.SHORT_DATE)))),
        entry("shortTermEndMonth.inputValue", Collections.singletonList("")),
        entry("shortTermEndYear.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_shortTerm_startInPast() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(yesterday, LocalDate.now());
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDay.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_AFTER
            .formatted(DateUtils.format(yesterday, DateUtils.SHORT_DATE)))),
        entry("shortTermStartMonth.inputValue", Collections.singletonList("")),
        entry("shortTermStartYear.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_shortTerm_startTooFarInFuture() {
    LocalDate sixMonthsAhead = LocalDate.now().plusMonths(6);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(sixMonthsAhead, sixMonthsAhead);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDay.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_BEFORE
            .formatted(DateUtils.format(sixMonthsAhead, DateUtils.SHORT_DATE)))),
        entry("shortTermStartMonth.inputValue", Collections.singletonList("")),
        entry("shortTermStartYear.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_shortTerm_startAndEndTooFarInFuture() {
    LocalDate sixMonthsAhead = LocalDate.now().plusMonths(6);
    LocalDate sevenMonthsAhead = LocalDate.now().plusMonths(7);
    LocalDate eighteenMonthsAhead = LocalDate.now().plusMonths(18);
    LocalDate nineteenMonthsAhead = LocalDate.now().plusMonths(19);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(sevenMonthsAhead, nineteenMonthsAhead);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDay.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_BEFORE
            .formatted(DateUtils.format(sixMonthsAhead, DateUtils.SHORT_DATE)))),
        entry("shortTermStartMonth.inputValue", Collections.singletonList("")),
        entry("shortTermStartYear.inputValue", Collections.singletonList("")),
        entry("shortTermEndDay.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_BEFORE
            .formatted(DateUtils.format(eighteenMonthsAhead.minusDays(2), DateUtils.SHORT_DATE)))),
        entry("shortTermEndMonth.inputValue", Collections.singletonList("")),
        entry("shortTermEndYear.inputValue", Collections.singletonList(""))
    );

  }


  @Test
  void validate_shortTerm_endInPast() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(startDate, yesterday);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDay.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_AFTER
            .formatted(DateUtils.format(startDate.minusDays(1), DateUtils.SHORT_DATE)))),
        entry("shortTermEndMonth.inputValue", Collections.singletonList("")),
        entry("shortTermEndYear.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_shortTerm_endInPast_blankStart() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(yesterday, yesterday);
    form.setShortTermStartDay("");
    form.setShortTermStartMonth("");
    form.setShortTermStartYear("");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDay.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_INCOMPLETE)),
        entry("shortTermStartMonth.inputValue", Collections.singletonList("")),
        entry("shortTermStartYear.inputValue", Collections.singletonList("")),
        entry("shortTermEndDay.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_AFTER
            .formatted(DateUtils.format(yesterday, DateUtils.SHORT_DATE)))),
        entry("shortTermEndMonth.inputValue", Collections.singletonList("")),
        entry("shortTermEndYear.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_shortTerm_endInPast_invalidStart() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(yesterday, yesterday);
    form.setShortTermStartDay("a");
    form.setShortTermStartMonth("a");
    form.setShortTermStartYear("a");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDay.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_INVALID)),
        entry("shortTermStartMonth.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_INVALID)),
        entry("shortTermStartYear.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_INVALID)),
        entry("shortTermEndDay.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_AFTER
            .formatted(DateUtils.format(yesterday, DateUtils.SHORT_DATE)))),
        entry("shortTermEndMonth.inputValue", Collections.singletonList("")),
        entry("shortTermEndYear.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_shortTerm_endBlank() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(startDate, yesterday);
    form.setShortTermEndDay("");
    form.setShortTermEndMonth("");
    form.setShortTermEndYear("");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDay.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_INCOMPLETE)),
        entry("shortTermEndMonth.inputValue", Collections.singletonList("")),
        entry("shortTermEndYear.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_shortTerm_endInvalid() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(startDate, yesterday);
    form.setShortTermEndDay("-1");
    form.setShortTermEndMonth("a");
    form.setShortTermEndYear("b");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDay.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_INVALID)),
        entry("shortTermEndMonth.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_INVALID)),
        entry("shortTermEndYear.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_INVALID))
    );

  }

  @Test
  void validate_annual_validForm() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_annual_blankYear() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    form.setAnnualConsentYear("");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_EMPTY))
    );

  }

  @Test
  void validate_annual_invalidYear() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    form.setAnnualConsentYear("a");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_INVALID))
    );

  }

  @Test
  void validate_annual_negativeYear() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    form.setAnnualConsentYear("-1");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_GT_EQUAL
            .formatted(Year.now().getValue() + 1)))
    );

  }


  @Test
  void validate_longTerm_validForm() {
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_longTerm_blankYears() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear);
    form.setLongTermStartYear("");
    form.setLongTermEndYear("");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermStartYear.inputValue", Collections.singletonList(LONG_TERM_START_YEAR_EMPTY)),
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_EMPTY))
    );

  }

  @Test
  void validate_longTerm_invalidYears() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear);
    form.setLongTermStartYear("a");
    form.setLongTermEndYear("a");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermStartYear.inputValue", Collections.singletonList(LONG_TERM_START_YEAR_INVALID)),
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_INVALID))
    );

  }

  @Test
  void validate_longTerm_invalidYearsNegative() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear);
    form.setLongTermStartYear("-1");
    form.setLongTermEndYear("-1");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermStartYear.inputValue", Collections.singletonList(LONG_TERM_START_YEAR_GT_EQUAL.formatted(currentYear))),
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_GT_EQUAL.formatted(currentYear + 1)))
    );

  }


  @Test
  void validate_longTerm_termTooShort() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_GT_EQUAL
            .formatted(currentYear + 1)))
    );

  }

  @Test
  void validate_longTerm_termMoreThanTenYears() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear + 10);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_LT_EQUAL
            .formatted(currentYear + 9)))
    );

  }

  @Test
  void validate_longTerm_validTenYearTerm() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear + 9);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_withConsentLengthTypeMissing() {
    ConsentLengthForm form = new ConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("consentLengthType", Collections.singletonList(CONSENT_LENGTH_PERIOD_EMPTY))
    );
  }

  @Test
  void validate_withAnnualConsentYearMissing() {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.ANNUAL);

    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_EMPTY))
    );
  }
}