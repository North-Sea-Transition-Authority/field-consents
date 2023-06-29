package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.DEADLINE_DATE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.DEADLINE_HOURS_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.DEADLINE_MINUTES_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.TECHNICAL_REVIEWER_EMPTY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.TECHNICAL_REVIEWER_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.validation.ValidatorUtils.EMPTY_STRING;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
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
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewRequestFormValidatorTest {

  private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();

  private static final Instant CURRENT_INSTANT = Instant.now();

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
      ValidatorUtils.DATE_TIME_HOURS_AHEAD_ERROR_MESSAGE.formatted(DEADLINE_INITCAP, "1", EMPTY_STRING);

  private static final Long TECHNICAL_REVIEWER_WUA_ID = 99L;

  private static final WebUserAccountId TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID
      = WebUserAccountId.from(TECHNICAL_REVIEWER_WUA_ID);

  private Errors errors;

  private TechnicalReviewRequestForm form;

  @Mock
  private Clock clock;

  @InjectMocks
  private TechnicalReviewRequestFormValidator formValidator;

  @BeforeEach
  void setUp() {
    form = new TechnicalReviewRequestForm();
  }

  @Test
  void supports() {
    assertThat(formValidator.supports(TechnicalReviewRequestForm.class)).isTrue();
  }

  @Test
  void supports_false() {
    assertThat(formValidator.supports(Object.class)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"Test", ""})
  void validate_whenValidForm_thenNoErrors(String requestText) {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(CURRENT_DATE_TIME.getHour() + 2));
    form.setDeadlineMinutes(String.valueOf(CURRENT_DATE_TIME.getMinute()));
    form.setRequestText(requestText);
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenTechnicalReviewerMissing_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(CURRENT_DATE_TIME.getHour() + 2));
    form.setDeadlineMinutes(String.valueOf(CURRENT_DATE_TIME.getMinute()));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(TECHNICAL_REVIEWER_FIELD_NAME, Collections.singletonList(TECHNICAL_REVIEWER_EMPTY))
    );
  }

  @Test
  void validate_whenDateInPastWithValidTime_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
  void validate_whenDateTimePlus1Hour_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    var nowPlus1Hour = CURRENT_DATE_TIME.plusHours(1);
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
    form.setDeadlineDate(DateUtils.format(nowPlus61Minutes.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(nowPlus61Minutes.getHour()));
    form.setDeadlineMinutes(String.valueOf(nowPlus61Minutes.getMinute()));
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_whenDateAndTimeMissing_thenError() {
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
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
    var nowPlus2Hour = CURRENT_DATE_TIME.plusHours(2);
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate().minusDays(1),
        DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours(String.valueOf(nowPlus2Hour.getHour()));
    form.setDeadlineMinutes(String.valueOf(nowPlus2Hour.getMinute()));
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
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
