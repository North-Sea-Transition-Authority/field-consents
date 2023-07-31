package uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

@ExtendWith(MockitoExtension.class)
class NeedsSubmittingFormValidatorTest {

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private final NeedsSubmittingFormValidator validator = new NeedsSubmittingFormValidator(CLOCK);

  @Test
  void supports() {
    assertThat(validator.supports(NeedsSubmittingForm.class)).isTrue();
  }

  @Test
  void validate_emptyRadioOnSubmission() {
    var form = new NeedsSubmittingForm(null, null, null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1)
        .first()
        .extracting(
            FieldError::getField,
            FieldError::getDefaultMessage
        ).containsExactly(
            "haveEiaDirectionToSubmit",
            "Select yes if you have an EIA screening direction to submit"
        );
  }

  @Test
  void validate_dateNotInFuture() {
    var form = new NeedsSubmittingForm(true, null, null);
    form.latestDateToBeSubmitted().setDate(LocalDate.now(CLOCK));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(3) // empty errors are added to the month and year, but we don't care about those
        .first()
        .extracting(FieldError::getField, FieldError::getDefaultMessage)
        .containsExactly("latestDateToBeSubmitted.dayInput.inputValue", "Submission date must be in the future");
  }

  @Test
  void validate_emptyReason() {
    var form = new NeedsSubmittingForm(false, null, null);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .hasSize(1) // empty errors are added to the month and year, but we don't care about those
        .first()
        .extracting(FieldError::getField, FieldError::getDefaultMessage)
        .containsExactly("whyNoEiaDirection.inputValue", "Enter an explanation");
  }

  @Test
  void validate_valid_needsSubmitting() {
    var form = new NeedsSubmittingForm(true, null, null);
    form.latestDateToBeSubmitted().setDate(LocalDate.now(CLOCK).plusWeeks(1));

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_valid_doesNotNeedSubmitting() {
    var form = new NeedsSubmittingForm(false, null, null);
    form.whyNoEiaDirection().setInputValue("reason");

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

}
