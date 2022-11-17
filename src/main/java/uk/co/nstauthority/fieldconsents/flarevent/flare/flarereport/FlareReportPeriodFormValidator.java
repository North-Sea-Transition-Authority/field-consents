package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.time.Month;
import java.time.YearMonth;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.integer.IntegerInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
class FlareReportPeriodFormValidator implements Validator {

  static final String HAS_DATA_FOR_PERIOD_EMPTY = "Select whether you have flare report data for the period";
  static final String REPORT_END_MONTH_IN_FUTURE = "Month cannot be in the future";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareReportPeriodForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareReportPeriodForm setupForm = (FlareReportPeriodForm) target;

    ValidationUtils.rejectIfEmpty(errors, "hasDataForPeriod",
        "hasDataForPeriod.required",
        HAS_DATA_FOR_PERIOD_EMPTY);

    // report data not available for the proposed report period so check the rest of the form
    if (Boolean.FALSE.equals(setupForm.getHasDataForPeriod())) {

      // ensure a year has been selected
      IntegerInputValidator.builder()
          .validate(setupForm.getReportEndYear(), errors);

      // just check the month has been selected
      StringInputValidator.builder()
          .validate(setupForm.getReportEndMonth(), errors);

      // if all data has been entered check that the month selected is not in the future
      if (!errors.hasErrors()) {
        var reportEndYear = setupForm.getReportEndYear().getAsInteger()
            .orElseThrow(NoSuchElementException::new);
        var reportEndMonth = Month.valueOf(setupForm.getReportEndMonth().getInputValue());
        var reportEndYearMonth = YearMonth.of(reportEndYear, reportEndMonth);

        // we put the error on the month here as the years given in the form
        // are never greater than the current year
        if (reportEndYearMonth.isAfter(YearMonth.now())) {
          errors.rejectValue("reportEndMonth.inputValue",
              "reportEndMonth.monthInFuture",
              REPORT_END_MONTH_IN_FUTURE);
        }
      }
    }
  }
}
