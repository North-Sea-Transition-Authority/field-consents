package uk.co.nstauthority.fieldconsents.flarevent;

import java.time.Month;
import java.time.YearMonth;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.integer.IntegerInputValidator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;

@Service
public class FlareVentReportPeriodFormValidator implements Validator {

  public static final String REPORT_END_MONTH_IN_FUTURE = "Month cannot be in the future";

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return FlareVentReportPeriodForm.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareVentReportPeriodForm setupForm = (FlareVentReportPeriodForm) target;

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
