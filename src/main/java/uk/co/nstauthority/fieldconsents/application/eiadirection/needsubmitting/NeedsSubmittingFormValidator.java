package uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Component
public class NeedsSubmittingFormValidator implements Validator {

  private final Clock clock;

  NeedsSubmittingFormValidator(Clock clock) {
    this.clock = clock;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return NeedsSubmittingForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    var form = (NeedsSubmittingForm) target;

    ValidationUtils.rejectIfEmpty(
        errors,
        "haveEiaDirectionToSubmit",
        "required",
        "Select yes if you have an EIA screening direction to submit"
    );

    if (errors.hasErrors()) {
      return;
    }

    if (Boolean.TRUE.equals(form.haveEiaDirectionToSubmit())) {
      ThreeFieldDateInputValidator.builder()
          .mustBeAfterDate(LocalDate.now(clock))
          .mustBeAfterDateErrorMessage("Submission date must be in the future")
          .validate(form.latestDateToBeSubmitted(), errors);
    } else {
      StringInputValidator.builder().validate(form.whyNoEiaDirection(), errors);
    }
  }

}
