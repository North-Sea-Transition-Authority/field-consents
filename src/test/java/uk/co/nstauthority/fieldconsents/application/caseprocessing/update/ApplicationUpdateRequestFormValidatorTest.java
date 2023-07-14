package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestFormValidator.DEADLINE_DATE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestFormValidator.DEADLINE_HOURS_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestFormValidator.DEADLINE_MINUTES_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestFormValidator.MUST_BE_HOURS_AHEAD;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.APPLICATION_UPDATE_REQUEST_TEXT;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_DATE_TIME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateTestUtil.CURRENT_INSTANT;
import static uk.co.nstauthority.fieldconsents.validation.ValidatorUtils.EMPTY_STRING;

import java.time.Clock;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@ExtendWith(MockitoExtension.class)
class ApplicationUpdateRequestFormValidatorTest {

  private static final String REQUEST_TEXT_FIELD_NAME = "requestText";

  private static final String REQUEST_TEXT_ERROR_MESSAGE = "Enter application update request details";

  private static final String DEADLINE_LOWER = "deadline";

  private static final String DEADLINE_INITCAP = StringUtils.capitalize(DEADLINE_LOWER);

  private static final String DEADLINE_REQUIRED_ERROR_MESSAGE =
      ValidatorUtils.DATE_REQUIRED_ERROR_MESSAGE.formatted(DEADLINE_LOWER);

  private static final String DEADLINE_BEFORE_TODAY_ERROR_MESSAGE =
      ValidatorUtils.DATE_BEFORE_TODAY_ERROR_MESSAGE.formatted(DEADLINE_INITCAP);

  private static final String DEADLINE_INVALID_ERROR_MESSAGE =
      ValidatorUtils.DATE_INVALID_ERROR_MESSAGE.formatted(DEADLINE_INITCAP);

  private static final String DEADLINE_TIME_REQUIRED_ERROR_MESSAGE =
      ValidatorUtils.TIME_REQUIRED_ERROR_MESSAGE.formatted(DEADLINE_LOWER);

  private static final String DEADLINE_TIME_INVALID_ERROR_MESSAGE =
      ValidatorUtils.TIME_INVALID_ERROR_MESSAGE.formatted(DEADLINE_INITCAP);

  private static final String DEADLINE_AHEAD_ERROR_MESSAGE =
      ValidatorUtils.DATE_TIME_HOURS_AHEAD_ERROR_MESSAGE.formatted(DEADLINE_INITCAP, MUST_BE_HOURS_AHEAD, EMPTY_STRING);

  private Errors errors;

  private ApplicationUpdateRequestForm form;

  @Mock
  private Clock clock;

  @InjectMocks
  private ApplicationUpdateRequestFormValidator formValidator;

  @BeforeEach
  void setUp() {
    form = new ApplicationUpdateRequestForm();
    form.setRequestText(APPLICATION_UPDATE_REQUEST_TEXT);
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(ApplicationUpdateRequestForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @Test
  void validate_whenValidForm_thenNoErrors() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(CURRENT_DATE_TIME.getHour() + 2));
    form.setDeadlineMinutes(String.valueOf(CURRENT_DATE_TIME.getMinute()));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenRequestTextMissing_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(CURRENT_DATE_TIME.getHour() + 2));
    form.setDeadlineMinutes(String.valueOf(CURRENT_DATE_TIME.getMinute()));
    form.setRequestText(null);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry("%s.inputValue".formatted(REQUEST_TEXT_FIELD_NAME), Collections.singletonList(REQUEST_TEXT_ERROR_MESSAGE))
    );
  }

  @Test
  void validate_whenDateInPastWithValidTime_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate().minusDays(1),
        DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(CURRENT_DATE_TIME.getHour() + 2));
    form.setDeadlineMinutes(String.valueOf(CURRENT_DATE_TIME.getMinute()));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_BEFORE_TODAY_ERROR_MESSAGE))
    );
  }

  @Test
  void validate_whenDateTimePlus1Hours_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var nowPlus1Hour = CURRENT_DATE_TIME.plusHours(MUST_BE_HOURS_AHEAD);
    form.setDeadlineDate(DateUtils.format(nowPlus1Hour.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(nowPlus1Hour.getHour()));
    form.setDeadlineMinutes(String.valueOf(nowPlus1Hour.getMinute()));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_AHEAD_ERROR_MESSAGE)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(EMPTY_STRING))
    );
  }

  @Test
  void validate_whenDateTimePlus61Minutes_thenValid() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var nowPlus61Minutes = CURRENT_DATE_TIME.plusMinutes(61);
    form.setDeadlineDate(DateUtils.format(nowPlus61Minutes.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(nowPlus61Minutes.getHour()));
    form.setDeadlineMinutes(String.valueOf(nowPlus61Minutes.getMinute()));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenDateAndTimeMissing_thenError() {
    form.setRequestText(APPLICATION_UPDATE_REQUEST_TEXT);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_REQUIRED_ERROR_MESSAGE)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_REQUIRED_ERROR_MESSAGE)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(EMPTY_STRING))
    );
  }

  @Test
  void validate_whenDateInPastAndTimeMissing_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate().minusDays(1),
        DateUtils.DATE_PICKER_FORMAT));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_BEFORE_TODAY_ERROR_MESSAGE)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_REQUIRED_ERROR_MESSAGE)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(EMPTY_STRING))
    );
  }

  @Test
  void validate_whenDateCurrentAndTimeMissing_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setRequestText(APPLICATION_UPDATE_REQUEST_TEXT);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_REQUIRED_ERROR_MESSAGE)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(EMPTY_STRING))
    );
  }

  @Test
  void validate_whenDateInPastTimePlus2Hours_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var nowPlus2Hours = CURRENT_DATE_TIME.plusHours(2);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate().minusDays(1),
        DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(nowPlus2Hours.getHour()));
    form.setDeadlineMinutes(String.valueOf(nowPlus2Hours.getMinute()));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_BEFORE_TODAY_ERROR_MESSAGE))
    );
  }

  @Test
  void validate_whenDateInvalidTimePlus2Hours_thenError() {
    var nowPlus2Hour = CURRENT_DATE_TIME.plusHours(2);
    form.setDeadlineDate("xx/06/2023");
    form.setDeadlineHours(String.valueOf(nowPlus2Hour.getHour()));
    form.setDeadlineMinutes(String.valueOf(nowPlus2Hour.getMinute()));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_INVALID_ERROR_MESSAGE))
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"x", "-1", "24"})
  void validate_whenDateCurrentAndHoursInvalid_thenError(String hoursStr) {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(hoursStr);
    form.setDeadlineMinutes("30");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_INVALID_ERROR_MESSAGE))
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"x", "-1", "60"})
  void validate_whenDateCurrentAndMinutesInvalid_thenError(String minutesStr) {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours("23");
    form.setDeadlineMinutes(minutesStr);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_INVALID_ERROR_MESSAGE))
    );
  }

  @Test
  void validate_whenDateInPastAndTimeInvalid_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate().minusDays(1),
        DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours("x");
    form.setDeadlineMinutes("30");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_BEFORE_TODAY_ERROR_MESSAGE)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_INVALID_ERROR_MESSAGE))
    );
  }

  @Test
  void validate_whenDateAndTimeInvalid_thenError() {
    form.setDeadlineDate("xx/06/2023");
    form.setDeadlineHours("24");
    form.setDeadlineMinutes("x");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_INVALID_ERROR_MESSAGE)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_INVALID_ERROR_MESSAGE)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(EMPTY_STRING))
    );
  }

  @Test
  void validate_whenHoursMissing_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineMinutes("30");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_REQUIRED_ERROR_MESSAGE))
    );
  }

  @Test
  void validate_whenMinutesMissing_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours("10");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_REQUIRED_ERROR_MESSAGE))
    );
  }
}
