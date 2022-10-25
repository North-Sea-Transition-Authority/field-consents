package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.integer.IntegerInputValidator;

@Service
public class ConsentLengthFormValidator implements Validator {

  private static final String CONSENT_LENGTH_PERIOD_EMPTY = "Select the period of the consent you are applying for";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return ConsentLengthForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    ConsentLengthForm form = (ConsentLengthForm) target;

    ValidationUtils.rejectIfEmpty(errors, "consentLengthType", "consentLengthType.required", CONSENT_LENGTH_PERIOD_EMPTY);

    ConsentLengthType type = form.getConsentLengthType();
    if (type != null) {
      switch (type) {
        case SHORT_TERM -> validateShortTermDetails(errors, form);
        case ANNUAL -> validateAnnualConsentYear(errors, form);
        case LONG_TERM -> validateLongTermDetails(errors, form);
        default -> throw new RuntimeException("Incorrect consent length type: " + type);
      }
    }
  }

  private void validateAnnualConsentYear(@NotNull Errors errors, ConsentLengthForm form) {
    IntegerInputValidator.builder().validate(form.getAnnualConsentYear(), errors);
  }

  private void validateLongTermDetails(@NotNull Errors errors, ConsentLengthForm form) {
    // Long term start year
    IntegerInputValidator.builder().validate(form.getLongTermStartYear(), errors);

    // Long term end year
    if (!form.getLongTermStartYear().fieldHasErrors(errors)) {
      var startYear = form.getLongTermStartYear().getAsInteger()
          .orElseThrow(NoSuchElementException::new);
      var endYearComparisonValidator = IntegerInputValidator.builder()
          .mustBeMoreThanOrEqual(startYear + 1);
      endYearComparisonValidator.validate(form.getLongTermEndYear(), errors);
    } else {
      IntegerInputValidator.builder().validate(form.getLongTermEndYear(), errors);
    }
  }

  private void validateShortTermDetails(@NotNull Errors errors, ConsentLengthForm form) {
    var startDateValidator = ThreeFieldDateInputValidator.builder();
    startDateValidator.validate(form.getShortTermStartDate(), errors);

    Optional<LocalDate> shortTermStartDate = form.getShortTermStartDate().getAsLocalDate();
    if (shortTermStartDate.isPresent()) {
      var endDateValidatorWithStartDate = ThreeFieldDateInputValidator.builder()
          .mustBeAfterDate(shortTermStartDate.get())
          .mustBeBeforeDate(shortTermStartDate.get().plusYears(1));
      endDateValidatorWithStartDate.validate(form.getShortTermEndDate(), errors);
    } else {
      var endDateValidator = ThreeFieldDateInputValidator.builder()
          .mustBeAfterDate(LocalDate.now());
      endDateValidator.validate(form.getShortTermEndDate(), errors);
    }

  }
}
