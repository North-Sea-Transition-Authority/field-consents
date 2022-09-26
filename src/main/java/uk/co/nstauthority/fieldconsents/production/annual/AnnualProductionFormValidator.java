package uk.co.nstauthority.fieldconsents.production.annual;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
public class AnnualProductionFormValidator implements Validator {

  private final AnnualProductionMonthFormValidator annualProductionMonthFormValidator;

  public AnnualProductionFormValidator(AnnualProductionMonthFormValidator annualProductionMonthFormValidator) {
    this.annualProductionMonthFormValidator = annualProductionMonthFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(AnnualProductionForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    AnnualProductionForm form = (AnnualProductionForm) target;

    List<AnnualProductionMonthForm> annualProductionMonthForms = form.getAnnualProductionMonthForms();

    // Validate each individual field for each annual production month form
    for (int index = 0; index < annualProductionMonthForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          annualProductionMonthFormValidator,
          "annualProductionMonthForms[" + index + "]",
          annualProductionMonthForms.get(index),
          errors
      );
    }
  }
}
