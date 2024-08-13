package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.LocalDate;
import java.time.Year;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.validator.date.ThreeFieldDateInputValidator;
import uk.co.fivium.formlibrary.validator.integer.IntegerInputValidator;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Component
public class ConsentLengthFormValidator {

  private static final String CONSENT_LENGTH_PERIOD_EMPTY = "Select the period of the consent you are applying for";

  public static final String SHORT_TERM_LONGER_THAN_ONE_YEAR = "The term must be less than 1 year";

  public static final String SHORT_TERM_END_DATE_BEFORE_START_DATE = "End date can be the same as the start date or after";

  public static final String SHORT_TERM_START_DATE_BEFORE_TODAY = "Start date can be today or after today";

  public static final String SHORT_TERM_START_DATE_AFTER_SIX_MONTHS = "Start date must not be more than 6 months into the future";

  public static final String LONG_TERM_START_YEAR_BEFORE_CURRENT_YEAR = "Start year can be the current year or after";

  public static final String LONG_TERM_END_YEAR_IS_CURRENT_YEAR_OR_BEFORE = "End year must be after the current year";

  public static final String LONG_TERM_END_YEAR_BEFORE_START_YEAR = "End year can be the same as the start year or after";

  public static final String LONG_TERM_END_YEAR_IS_START_YEAR_OR_BEFORE = "End year must be after the start year";

  public static final String LONG_TERM_INVALID_DURATION = "The term should between 2 and 30 years";

  private final ConsentLengthService consentLengthService;

  ConsentLengthFormValidator(ConsentLengthService consentLengthService) {
    this.consentLengthService = consentLengthService;
  }

  public void validate(ConsentLengthForm form, @NotNull Errors errors, ApplicationVersion applicationVersion) {
    var application = applicationVersion.getApplication();
    if (!application.isRevision()) {
      ValidationUtils.rejectIfEmpty(errors, "consentLengthType", "consentLengthType.required", CONSENT_LENGTH_PERIOD_EMPTY);

      var type = form.getConsentLengthType();
      if (type != null) {
        switch (type) {
          case SHORT_TERM -> validateShortTermDetails(errors, form);
          case ANNUAL -> validateAnnualConsentYear(errors, form);
          case LONG_TERM -> validateLongTermDetails(errors, form, application);
          default -> throw new IllegalStateException("Incorrect consent length type: " + type);
        }
      }
    } else {
      // Only the short term end date and long term end year are editable for revisions.
      var consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

      var type = consentLengthDetails.getConsentLength();
      switch (type) {
        case SHORT_TERM -> validateShortTermEndDate(errors, form, consentLengthDetails.getShortTermStartDate());
        case ANNUAL -> {
        }
        case LONG_TERM -> validateLongTermEndYear(errors, form, consentLengthDetails.getLongTermStartYear(), application);
        default -> throw new IllegalStateException("Incorrect consent length type: " + type);
      }
    }
  }

  private void validateAnnualConsentYear(@NotNull Errors errors, ConsentLengthForm form) {
    // annual must start in the following year
    IntegerInputValidator.builder()
        .emptyInputErrorMessage("Select a year")
        .mustBeMoreThanOrEqualTo(Year.now().getValue() + 1)
        .validate(form.getAnnualConsentYear(), errors);
  }

  private void validateLongTermDetails(@NotNull Errors errors, ConsentLengthForm form, Application application) {
    int currentYear = Year.now().getValue();

    // Long term start year
    IntegerInputValidator.builder()
        .emptyInputErrorMessage("Select a start year")
        .mustBeMoreThanOrEqualTo(currentYear)
        .mustBeMoreThanOrEqualToErrorMessage(LONG_TERM_START_YEAR_BEFORE_CURRENT_YEAR)
        .validate(form.getLongTermStartYear(), errors);

    // Long term end year
    if (!form.getLongTermStartYear().fieldHasErrors(errors)) {
      var startYear = form.getLongTermStartYear().getAsInteger()
          .orElseThrow(NoSuchElementException::new);

      validateLongTermEndYear(errors, form, startYear, application);
    } else {
      IntegerInputValidator.builder()
          .mustBeMoreThanOrEqualTo(currentYear + 1)
          .mustBeMoreThanOrEqualToErrorMessage(LONG_TERM_END_YEAR_IS_CURRENT_YEAR_OR_BEFORE)
          .validate(form.getLongTermEndYear(), errors);
    }
  }

  private void validateLongTermEndYear(@NotNull Errors errors, ConsentLengthForm form, int startYear, Application application) {
    // the term must be a minimum of 2 years and a maximum of 30 years in duration
    var endYearComparisonValidator = IntegerInputValidator.builder()
        .mustBeMoreThanOrEqualTo(
            !application.isRevision()
                ? startYear + 1
                : startYear
        )
        .mustBeMoreThanOrEqualToErrorMessage(
            !application.isRevision()
                ? LONG_TERM_END_YEAR_IS_START_YEAR_OR_BEFORE
                : LONG_TERM_END_YEAR_BEFORE_START_YEAR
        )
        .mustBeLessThanOrEqualTo(startYear + 29)
        .mustBeLessThanOrEqualToErrorMessage(LONG_TERM_INVALID_DURATION);
    endYearComparisonValidator.validate(form.getLongTermEndYear(), errors);
  }

  private void validateShortTermDetails(@NotNull Errors errors, ConsentLengthForm form) {
    LocalDate today = LocalDate.now();
    LocalDate sixMonthsAhead = today.plusMonths(6);
    // the start date can be the current date or after but not more than 6 months into the future
    var startDateValidator = ThreeFieldDateInputValidator.builder()
        .mustBeAfterOrEqualTo(today)
        .mustBeAfterOrEqualToErrorMessage(SHORT_TERM_START_DATE_BEFORE_TODAY)
        .mustBeBeforeOrEqualTo(sixMonthsAhead)
        .mustBeBeforeOrEqualToErrorMessage(SHORT_TERM_START_DATE_AFTER_SIX_MONTHS);
    startDateValidator.validate(form.getShortTermStartDate(), errors);

    Optional<LocalDate> shortTermStartDate = form.getShortTermStartDate().getAsLocalDate();
    if (shortTermStartDate.isPresent()) {
      var startDate = shortTermStartDate.get();

      validateShortTermEndDate(errors, form, startDate);
    } else {
      var endDateValidator = ThreeFieldDateInputValidator.builder()
          .mustBeAfterOrEqualTo(today);
      endDateValidator.validate(form.getShortTermEndDate(), errors);
    }
  }

  private void validateShortTermEndDate(@NotNull Errors errors, ConsentLengthForm form, LocalDate startDate) {
    LocalDate today = LocalDate.now();
    LocalDate sixMonthsAhead = today.plusMonths(6);

    var endDateValidatorWithStartDate = ThreeFieldDateInputValidator.builder()
        // the end date can be the current date or after, but must also be the same as or after the start date
        // we do a maximum on the dates here to find the latest acceptable start date
        // the start date entered may be before today (i.e. not valid)
        .mustBeAfterOrEqualTo(DateUtils.max(today, startDate))
        .mustBeAfterOrEqualToErrorMessage(SHORT_TERM_END_DATE_BEFORE_START_DATE)
        // term less than 1 year
        // we do a minimum on the dates here to find the earliest acceptable start date
        // the start date entered may be too far in the future (i.e. not valid)
        .mustBeBeforeOrEqualTo(
            DateUtils.min(sixMonthsAhead, startDate)
                .plusYears(1).minusDays(2)
        )
        .mustBeBeforeOrEqualToErrorMessage(SHORT_TERM_LONGER_THAN_ONE_YEAR);
    endDateValidatorWithStartDate.validate(form.getShortTermEndDate(), errors);
  }
}
