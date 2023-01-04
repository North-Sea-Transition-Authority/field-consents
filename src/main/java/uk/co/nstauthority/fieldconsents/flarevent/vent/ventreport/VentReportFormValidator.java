package uk.co.nstauthority.fieldconsents.flarevent.vent.ventreport;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
class VentReportFormValidator implements Validator {

  private final VentReportMonthFormValidator ventReportMonthFormValidator;

  @Autowired
  VentReportFormValidator(VentReportMonthFormValidator ventReportMonthFormValidator) {
    this.ventReportMonthFormValidator = ventReportMonthFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(VentReportForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    VentReportForm form = (VentReportForm) target;

    List<VentReportMonthForm> ventReportMonthForms = form.getVentReportMonthForms();

    // Validate each individual field for each vent report month form
    for (int index = 0; index < ventReportMonthForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          ventReportMonthFormValidator,
          "ventReportMonthForms[" + index + "]",
          ventReportMonthForms.get(index),
          errors
      );
    }
  }
}
