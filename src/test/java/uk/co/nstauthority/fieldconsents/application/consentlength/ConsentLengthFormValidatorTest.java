package uk.co.nstauthority.fieldconsents.application.consentlength;

import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.LONG_TERM_END_YEAR_BEFORE_START_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.LONG_TERM_END_YEAR_IS_CURRENT_YEAR_OR_BEFORE;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.LONG_TERM_END_YEAR_IS_START_YEAR_OR_BEFORE;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.LONG_TERM_INVALID_DURATION;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.LONG_TERM_START_YEAR_BEFORE_CURRENT_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.SHORT_TERM_END_DATE_BEFORE_START_DATE;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.SHORT_TERM_LONGER_THAN_ONE_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.SHORT_TERM_START_DATE_AFTER_SIX_MONTHS;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthFormValidator.SHORT_TERM_START_DATE_BEFORE_TODAY;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.LONG_TERM_END_YEAR;
import static uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthTestUtil.LONG_TERM_START_YEAR;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class ConsentLengthFormValidatorTest {

  private static final String CONSENT_LENGTH_PERIOD_EMPTY = "Select the period of the consent you are applying for";

  private static final String ANNUAL_CONSENT_YEAR_EMPTY = "Select a year";

  private static final String ANNUAL_CONSENT_YEAR_INVALID = "A year must be a whole number";

  private static final String ANNUAL_CONSENT_YEAR_GT_EQUAL = "A year must be %s or more";

  private static final String SHORT_TERM_START_DATE_INCOMPLETE = "Enter a complete start date";

  private static final String SHORT_TERM_START_DATE_INVALID = "Start date must be a real date";

  private static final String SHORT_TERM_END_DATE_AFTER = "End date must be the same as or after %s";

  private static final String SHORT_TERM_END_DATE_INCOMPLETE = "Enter a complete end date";

  private static final String SHORT_TERM_END_DATE_INVALID = "End date must be a real date";

  private static final String LONG_TERM_START_YEAR_EMPTY = "Select a start year";

  private static final String LONG_TERM_START_YEAR_INVALID = "A start year must be a whole number";

  private static final String LONG_TERM_END_YEAR_EMPTY = "Enter an end year";

  private static final String LONG_TERM_END_YEAR_INVALID = "An end year must be a whole number";

  @Mock
  private ConsentLengthService consentLengthService;

  @InjectMocks
  private ConsentLengthFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_validForm() {
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(LocalDate.now(), LocalDate.now());
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_validForm_termJustLessThanOneYear() {
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(LocalDate.now(),
            LocalDate.now().plusYears(1).minusDays(2));
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_termNotLessThanOneYear() {
    LocalDate startDate = LocalDate.now().plusDays(10);
    // if the start date was 1st Jan 2022 here the end date would be 31st Dec 2022
    LocalDate endDate = startDate.plusYears(1).minusDays(1);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(startDate, endDate);
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_LONGER_THAN_ONE_YEAR)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_startInPast() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate today = LocalDate.now();
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(yesterday, today);
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_BEFORE_TODAY)),
        entry("shortTermStartDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermStartDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_startTooFarInFuture() {
    LocalDate sixMonthsAhead = LocalDate.now().plusMonths(6);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(sixMonthsAhead.plusDays(1), sixMonthsAhead.plusDays(1));
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_AFTER_SIX_MONTHS)),
        entry("shortTermStartDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermStartDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_startAndEndTooFarInFuture() {
    LocalDate sevenMonthsAhead = LocalDate.now().plusMonths(7);
    LocalDate nineteenMonthsAhead = LocalDate.now().plusMonths(19);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(sevenMonthsAhead, nineteenMonthsAhead);
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_AFTER_SIX_MONTHS)),
        entry("shortTermStartDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermStartDate.yearInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_LONGER_THAN_ONE_YEAR)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_endInPast() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(startDate, yesterday);
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_BEFORE_START_DATE)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_endInPast_blankStart() {
    LocalDate today = LocalDate.now();
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(today, today.minusDays(1));
    form.getShortTermStartDate().getDayInput().setInputValue("");
    form.getShortTermStartDate().getMonthInput().setInputValue("");
    form.getShortTermStartDate().getYearInput().setInputValue("");

    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_INCOMPLETE)),
        entry("shortTermStartDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermStartDate.yearInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_AFTER
            .formatted(DateUtils.format(today, DateUtils.LONG_DATE)))),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_endInPast_invalidStart() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(yesterday, yesterday);
    form.getShortTermStartDate().getDayInput().setInputValue("a");
    form.getShortTermStartDate().getMonthInput().setInputValue("a");
    form.getShortTermStartDate().getYearInput().setInputValue("a");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermStartDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_START_DATE_INVALID)),
        entry("shortTermStartDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermStartDate.yearInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_AFTER
            .formatted(DateUtils.format(LocalDate.now(), DateUtils.LONG_DATE)))),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_endBlank() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(startDate, yesterday);
    form.getShortTermEndDate().getDayInput().setInputValue("");
    form.getShortTermEndDate().getMonthInput().setInputValue("");
    form.getShortTermEndDate().getYearInput().setInputValue("");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_INCOMPLETE)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_shortTerm_endInvalid() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(startDate, yesterday);
    form.getShortTermEndDate().getDayInput().setInputValue("-1");
    form.getShortTermEndDate().getMonthInput().setInputValue("a");
    form.getShortTermEndDate().getYearInput().setInputValue("b");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_INVALID)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsNotRevision_annual_validForm() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsNotRevision_annual_blankYear() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    form.getAnnualConsentYear().setInputValue("");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_EMPTY))
    );

  }

  @Test
  void validate_applicationIsNotRevision_annual_invalidYear() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    form.getAnnualConsentYear().setInputValue("a");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_INVALID))
    );

  }

  @Test
  void validate_applicationIsNotRevision_annual_negativeYear() {
    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthForm();
    form.getAnnualConsentYear().setInputValue("-1");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_GT_EQUAL
            .formatted(Year.now().getValue() + 1)))
    );

  }

  @Test
  void validate_applicationIsNotRevision_longTerm_validForm() {
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsNotRevision_longTerm_blankYears() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear);
    form.getLongTermStartYear().setInputValue("");
    form.getLongTermEndYear().setInputValue("");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermStartYear.inputValue", Collections.singletonList(LONG_TERM_START_YEAR_EMPTY)),
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_EMPTY))
    );

  }

  @Test
  void validate_applicationIsNotRevision_longTerm_invalidYears() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear);
    form.getLongTermStartYear().setInputValue("a");
    form.getLongTermEndYear().setInputValue("a");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermStartYear.inputValue", Collections.singletonList(LONG_TERM_START_YEAR_INVALID)),
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_INVALID))
    );

  }

  @Test
  void validate_applicationIsNotRevision_longTerm_invalidYearsNegative() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear);
    form.getLongTermStartYear().setInputValue("-1");
    form.getLongTermEndYear().setInputValue("-1");
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermStartYear.inputValue", Collections.singletonList(LONG_TERM_START_YEAR_BEFORE_CURRENT_YEAR)),
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_IS_CURRENT_YEAR_OR_BEFORE))
    );

  }

  @ParameterizedTest
  @MethodSource("getInvalidLongTermYearsForInitialApplication")
  void validate_applicationIsNotRevision_longTerm_endYearIsStartYearOrBefore(int startYear, int endYear) {
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(startYear, endYear);
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_IS_START_YEAR_OR_BEFORE))
    );
  }

  private static Stream<Arguments> getInvalidLongTermYearsForInitialApplication() {
    return Stream.of(
        Arguments.of(Year.now().getValue(), Year.now().getValue()), // start and end the same
        Arguments.of(Year.now().getValue(), Year.now().getValue() - 1), // end before start
        Arguments.of(Year.now().getValue() + 4, Year.now().getValue()) // start in future and end before start
    );
  }

  @Test
  void validate_applicationIsNotRevision_longTerm_noStartYear_endYearIsCurrent() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.LONG_TERM);
    form.getLongTermEndYear().setInteger(currentYear);
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsExactly(
        entry("longTermStartYear.inputValue", Collections.singletonList(LONG_TERM_START_YEAR_EMPTY)),
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_IS_CURRENT_YEAR_OR_BEFORE))
    );
  }

  @Test
  void validate_applicationIsNotRevision_longTerm_termMoreThanThirtyYears() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear + 30);
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_INVALID_DURATION))
    );

  }

  @Test
  void validate_applicationIsNotRevision_longTerm_validThirtyYearTerm() {
    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(currentYear, currentYear + 29);
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsNotRevision_withConsentLengthTypeMissing() {
    ConsentLengthForm form = new ConsentLengthForm();
    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("consentLengthType", Collections.singletonList(CONSENT_LENGTH_PERIOD_EMPTY))
    );
  }

  @Test
  void validate_applicationIsNotRevision_withAnnualConsentYearMissing() {
    ConsentLengthForm form = new ConsentLengthForm();
    form.setConsentLengthType(ConsentLengthType.ANNUAL);

    errors = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, errors, applicationVersion);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("annualConsentYear.inputValue", Collections.singletonList(ANNUAL_CONSENT_YEAR_EMPTY))
    );
  }

  @Test
  void validate_applicationIsRevision_shortTerm_validForm() {
    applicationVersion.getApplication().setVariationNo(1);

    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(null, LocalDate.now());
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(LocalDate.now());

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsRevision_shortTerm_validForm_termJustLessThanOneYear() {
    applicationVersion.getApplication().setVariationNo(1);

    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(null,
            LocalDate.now().plusYears(1).minusDays(2));
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(LocalDate.now());

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsRevision_shortTerm_termNotLessThanOneYear() {
    applicationVersion.getApplication().setVariationNo(1);

    LocalDate startDate = LocalDate.now().plusDays(10);
    // if the start date was 1st Jan 2022 here the end date would be 31st Dec 2022
    LocalDate endDate = startDate.plusYears(1).minusDays(1);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(null, endDate);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(startDate);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_LONGER_THAN_ONE_YEAR)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsRevision_shortTerm_endTooFarInFuture() {
    applicationVersion.getApplication().setVariationNo(1);

    LocalDate nineteenMonthsAhead = LocalDate.now().plusMonths(19);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(null, nineteenMonthsAhead);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(startDate);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_LONGER_THAN_ONE_YEAR)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsRevision_shortTerm_endInPast() {
    applicationVersion.getApplication().setVariationNo(1);

    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(null, yesterday);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(startDate);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_BEFORE_START_DATE)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsRevision_shortTerm_endBlank() {
    applicationVersion.getApplication().setVariationNo(1);

    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(null, yesterday);
    form.getShortTermEndDate().getDayInput().setInputValue("");
    form.getShortTermEndDate().getMonthInput().setInputValue("");
    form.getShortTermEndDate().getYearInput().setInputValue("");
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(startDate);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_INCOMPLETE)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsRevision_shortTerm_endInvalid() {
    applicationVersion.getApplication().setVariationNo(1);

    LocalDate yesterday = LocalDate.now().minusDays(1);
    LocalDate startDate = LocalDate.now().plusDays(10);
    ConsentLengthForm form =
        ConsentLengthTestUtil.getShortTermConsentLengthFormForDates(null, yesterday);
    form.getShortTermEndDate().getDayInput().setInputValue("-1");
    form.getShortTermEndDate().getMonthInput().setInputValue("a");
    form.getShortTermEndDate().getYearInput().setInputValue("b");
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);
    consentLengthDetails.setShortTermStartDate(startDate);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("shortTermEndDate.dayInput.inputValue", Collections.singletonList(SHORT_TERM_END_DATE_INVALID)),
        entry("shortTermEndDate.monthInput.inputValue", Collections.singletonList("")),
        entry("shortTermEndDate.yearInput.inputValue", Collections.singletonList(""))
    );

  }

  @Test
  void validate_applicationIsRevision_annual_validForm() {
    applicationVersion.getApplication().setVariationNo(1);

    ConsentLengthForm form = ConsentLengthTestUtil.getAnnualConsentLengthFormForYear(null);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.ANNUAL);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsRevision_longTerm_validForm() {
    applicationVersion.getApplication().setVariationNo(1);

    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(null, LONG_TERM_END_YEAR);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(LONG_TERM_START_YEAR);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_applicationIsRevision_longTerm_blankEndYear() {
    applicationVersion.getApplication().setVariationNo(1);

    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(null, currentYear);
    form.getLongTermEndYear().setInputValue("");
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(currentYear);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_EMPTY))
    );

  }

  @Test
  void validate_applicationIsRevision_longTerm_invalidEndYear() {
    applicationVersion.getApplication().setVariationNo(1);

    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(null, currentYear);
    form.getLongTermEndYear().setInputValue("a");
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(currentYear);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_INVALID))
    );

  }

  @Test
  void validate_applicationIsRevision_longTerm_invalidEndYearNegative() {
    applicationVersion.getApplication().setVariationNo(1);

    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(null, currentYear);
    form.getLongTermEndYear().setInputValue("-1");
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(currentYear);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_BEFORE_START_YEAR))
    );

  }

  @Test
  void validate_applicationIsRevision_longTerm_endYearIsStartYear() {
    applicationVersion.getApplication().setVariationNo(1);

    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(null, currentYear);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(currentYear);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("getInvalidLongTermYearsForRevisionApplication")
  void validate_applicationIsRevision_longTerm_endYearIsBeforeStartYear(int startYear, int endYear) {
    applicationVersion.getApplication().setVariationNo(1);

    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(null, endYear);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(startYear);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_END_YEAR_BEFORE_START_YEAR))
    );
  }

  private static Stream<Arguments> getInvalidLongTermYearsForRevisionApplication() {
    return Stream.of(
        Arguments.of(Year.now().getValue(), Year.now().getValue() - 1), // end before start
        Arguments.of(Year.now().getValue() + 4, Year.now().getValue()) // start in future and end before start
    );
  }

  @Test
  void validate_applicationIsRevision_longTerm_termMoreThanThirtyYears() {
    applicationVersion.getApplication().setVariationNo(1);

    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(null, currentYear + 30);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(currentYear);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isTrue();

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    Assertions.assertThat(errorMap).containsOnly(
        entry("longTermEndYear.inputValue", Collections.singletonList(LONG_TERM_INVALID_DURATION))
    );

  }

  @Test
  void validate_applicationIsRevision_longTerm_validThirtyYearTerm() {
    applicationVersion.getApplication().setVariationNo(1);

    int currentYear = Year.now().getValue();
    ConsentLengthForm form = ConsentLengthTestUtil.getLongTermConsentLengthFormForYears(null, currentYear + 29);
    errors = new BeanPropertyBindingResult(form, "form");

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);
    consentLengthDetails.setLongTermStartYear(currentYear);

    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);

    validator.validate(form, errors, applicationVersion);

    assertThat(errors.hasErrors()).isFalse();
  }
}
