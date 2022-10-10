package uk.co.nstauthority.fieldconsents.production.shortterm;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.production.annual.ProductionMonthFormValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
public class ShortTermProductionFormValidator implements Validator {
  
  private final ProductionMonthFormValidator productionMonthFormValidator;

  @Autowired
  public ShortTermProductionFormValidator(ProductionMonthFormValidator productionMonthFormValidator) {
    this.productionMonthFormValidator = productionMonthFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(ShortTermProductionForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    ShortTermProductionForm form = (ShortTermProductionForm) target;

    List<ShortTermProductionMonthForm> shortTermProductionMonthForms = form.getShortTermProductionMonthForms();

    // Validate each individual field for each short term production month form
    for (int index = 0; index < shortTermProductionMonthForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          productionMonthFormValidator,
          "shortTermProductionMonthForms[" + index + "]",
          shortTermProductionMonthForms.get(index),
          errors
      );
    }
  }
}
