package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

class ConsultationRequestFormValidatorTest {

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneOffset.systemDefault());

  @InjectMocks
  private ConsultationRequestFormValidator validator;

  @BeforeEach
  void setUp() {
    validator = new ConsultationRequestFormValidator(CLOCK);
  }

  @Test
  void supports() {
    assertThat(validator.supports(ConsultationRequestForm.class)).isTrue();
  }

  @Test
  void validate_emptyForm() {
    var form = ConsultationRequestForm.empty();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        ).containsExactly(
            tuple("deadlineDate", "deadlineDate.required", "Pick a deadline date"),
            tuple("deadlineHours", "deadlineHours.required", "Enter the deadline time"),
            tuple("deadlineMinutes", "deadlineMinutes.required", "")
        );
  }

  @Test
  void validate_dateInNotFarEnoughInTheFuture() {
    var futureInstant = CLOCK.instant().plus(1, ChronoUnit.HOURS).minusNanos(1);
    var form = getForm(futureInstant);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        ).containsExactly(
            tuple("deadlineHours", "deadlineHours.beforeDateTime", "Deadline must be at least 1 hour ahead of now"),
            tuple("deadlineMinutes", "deadlineMinutes.beforeDateTime", "")
        );
  }

  @Test
  void validate_dateExactlyOneHourInTheFuture() {
    var futureInstant = CLOCK.instant().plus(1, ChronoUnit.HOURS);
    var form = getForm(futureInstant);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(
            FieldError::getField,
            FieldError::getCode,
            FieldError::getDefaultMessage
        ).containsExactly(
            tuple("deadlineHours", "deadlineHours.beforeDateTime", "Deadline must be at least 1 hour ahead of now"),
            tuple("deadlineMinutes", "deadlineMinutes.beforeDateTime", "")
        );
  }

  @Test
  void validate_dateOneDayInTheFuture() {
    var futureInstant = CLOCK.instant().plus(1, ChronoUnit.DAYS);
    var form = getForm(futureInstant);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors()).isEmpty();
  }

  private static ConsultationRequestForm getForm(Instant instant) {
    var date = Date.from(instant);
    return new ConsultationRequestForm(
        DateFormatUtils.format(date, "dd/MM/yyyy"),
        DateFormatUtils.format(date, "HH"),
        DateFormatUtils.format(date, "mm")
    );
  }

}
