package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
class FlareReportFormValidator implements Validator {

  private final FlareReportMonthFormValidator flareReportMonthFormValidator;

  @Autowired
  FlareReportFormValidator(FlareReportMonthFormValidator flareReportMonthFormValidator) {
    this.flareReportMonthFormValidator = flareReportMonthFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(FlareReportForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    FlareReportForm form = (FlareReportForm) target;

    List<FlareReportMonthForm> flareReportMonthForms = form.getFlareReportMonthForms();

    // Validate each individual field for each flare report month form
    for (int index = 0; index < flareReportMonthForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          flareReportMonthFormValidator,
          "flareReportMonthForms[" + index + "]",
          flareReportMonthForms.get(index),
          errors
      );
    }
  }
}
