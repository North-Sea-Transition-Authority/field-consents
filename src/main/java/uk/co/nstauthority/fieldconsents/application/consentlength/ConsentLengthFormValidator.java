package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.LocalDate;
import java.time.Year;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.integer.IntegerInputValidator;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

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
    // annual must start in the following year
    IntegerInputValidator.builder()
        .mustBeMoreThanOrEqual(Year.now().getValue() + 1)
        .validate(form.getAnnualConsentYear(), errors);
  }

  private void validateLongTermDetails(@NotNull Errors errors, ConsentLengthForm form) {
    // TODO FCS-263 and DFL-35 change below to add custom errors messages when DFL updated, i.e.
    // 1) Start year can be the current year or after
    // 2) End year must be after the start year
    // 3) The term should between 2 and 30 years
    int currentYear = Year.now().getValue();

    // Long term start year
    IntegerInputValidator.builder()
        .mustBeMoreThanOrEqual(currentYear)
        .validate(form.getLongTermStartYear(), errors);

    // Long term end year
    if (!form.getLongTermStartYear().fieldHasErrors(errors)) {
      var startYear = form.getLongTermStartYear().getAsInteger()
          .orElseThrow(NoSuchElementException::new);

      // the term must be a minimum of 2 years and a maximum of 30 years in duration
      var endYearComparisonValidator = IntegerInputValidator.builder()
          .mustBeMoreThanOrEqual(startYear + 1)
          .mustBeLessThanOrEqualTo(startYear + 29);
      endYearComparisonValidator.validate(form.getLongTermEndYear(), errors);
    } else {
      IntegerInputValidator.builder()
          .mustBeMoreThanOrEqual(currentYear + 1)
          .validate(form.getLongTermEndYear(), errors);
    }
  }

  private void validateShortTermDetails(@NotNull Errors errors, ConsentLengthForm form) {
    // TODO FCS-263 and DFL-35 change below to add custom errors messages when DFL updated, i.e.
    // 1) Start date can be today or after today but not more than 6 months into the future
    // 2) End date can be the same as the start date or after
    // 3) The term should be less than 1 year
    LocalDate today = LocalDate.now();
    LocalDate sixMonthsAhead = today.plusMonths(6);
    // the start date can be the current date or after but not more than 6 months into the future
    var startDateValidator = ThreeFieldDateInputValidator.builder()
        .mustBeAfterOrEqualTo(today)
        .mustBeBeforeOrEqualTo(sixMonthsAhead);
    startDateValidator.validate(form.getShortTermStartDate(), errors);

    Optional<LocalDate> shortTermStartDate = form.getShortTermStartDate().getAsLocalDate();
    if (shortTermStartDate.isPresent()) {
      var endDateValidatorWithStartDate = ThreeFieldDateInputValidator.builder()
          // the end date can be the current date or after, but must also be the same as or after the start date
          // we do a maximum on the dates here to find the latest acceptable start date
          // the start date entered may be before today (i.e. not valid)
          .mustBeAfterOrEqualTo(DateUtils.max(today, shortTermStartDate.get()))
          // term less than 1 year
          // we do a minimum on the dates here to find the earliest acceptable start date
          // the start date entered may be too far in the future (i.e. not valid)
          .mustBeBeforeOrEqualTo(
              DateUtils.min(sixMonthsAhead, shortTermStartDate.get())
                  .plusYears(1).minusDays(2)
          );
      endDateValidatorWithStartDate.validate(form.getShortTermEndDate(), errors);
    } else {
      var endDateValidator = ThreeFieldDateInputValidator.builder()
          .mustBeAfterOrEqualTo(today);
      endDateValidator.validate(form.getShortTermEndDate(), errors);
    }

  }
}
