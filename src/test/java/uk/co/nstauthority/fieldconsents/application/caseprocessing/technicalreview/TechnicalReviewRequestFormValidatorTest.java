package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.DEADLINE_DATE_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.DEADLINE_HOURS_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.DEADLINE_MINUTES_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.TECHNICAL_REVIEWER_EMPTY;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewRequestFormValidator.TECHNICAL_REVIEWER_FIELD_NAME;

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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewRequestFormValidatorTest {

  private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();

  private static final Instant CURRENT_INSTANT = Instant.now();

  private static final String DEADLINE_DATE_EMPTY = "Pick a deadline date";

  private static final String DEADLINE_DATE_IN_PAST = "Deadline date must be on or after today";

  private static final String DEADLINE_DATE_TIME_NOT_AHEAD = "Deadline must be at least 1 hour ahead of now";

  private static final String DEADLINE_DATE_TIME_NOT_VALID =
      "Deadline must be a valid date in the format dd/mm/yyyy and time in hours and minutes";

  private static final String DEADLINE_TIME_EMPTY = "Enter the deadline time";
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
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_IN_PAST)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_AHEAD)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
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
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_AHEAD)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
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
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_EMPTY)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_EMPTY)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
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
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_IN_PAST)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_EMPTY)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
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
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_TIME_EMPTY)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
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
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_IN_PAST)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_AHEAD)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
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
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_VALID)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_VALID)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
    );
  }

  @Test
  void validate_whenDateCurrentAndTimeInvalid_thenError() {
    when(clock.instant()).thenReturn(CURRENT_INSTANT);
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
    form.setDeadlineDate(DateUtils.format(CURRENT_DATE_TIME.toLocalDate(), DateUtils.DATE_PICKER_FORMAT));
    form.setDeadlineHours("x");
    form.setDeadlineMinutes("30");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_VALID)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
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
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_IN_PAST)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_VALID)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
    );
  }

  @Test
  void validate_whenDateAndTimeInvalid_thenError() {
    form.setTechnicalReviewerWuaId(TECHNICAL_REVIEWER_WEB_USER_ACCOUNT_ID);
    form.setDeadlineDate("xx/06/2023");
    form.setDeadlineHours("12");
    form.setDeadlineMinutes("x");
    errors = new BeanPropertyBindingResult(form, "form");

    ValidationUtils.invokeValidator(formValidator, form, errors);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(DEADLINE_DATE_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_VALID)),
        entry(DEADLINE_HOURS_FIELD_NAME, Collections.singletonList(DEADLINE_DATE_TIME_NOT_VALID)),
        entry(DEADLINE_MINUTES_FIELD_NAME, Collections.singletonList(""))
    );
  }
}
