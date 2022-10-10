package uk.co.nstauthority.fieldconsents.production.longterm;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.production.ProductionRowFormValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Service
public class LongTermProductionFormValidator implements Validator {
  
  private final ProductionRowFormValidator productionRowFormValidator;

  @Autowired
  public LongTermProductionFormValidator(ProductionRowFormValidator productionRowFormValidator) {
    this.productionRowFormValidator = productionRowFormValidator;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(LongTermProductionForm.class);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    LongTermProductionForm form = (LongTermProductionForm) target;

    List<LongTermProductionYearForm> longTermProductionYearForms = form.getLongTermProductionYearForms();

    // Validate each individual field for each long term production year form
    for (int index = 0; index < longTermProductionYearForms.size(); index++) {
      ValidatorUtils.invokeNestedValidator(
          errors,
          productionRowFormValidator,
          "longTermProductionYearForms[" + index + "]",
          longTermProductionYearForms.get(index),
          errors
      );
    }
  }
}
