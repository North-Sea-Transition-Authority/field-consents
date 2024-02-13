package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.decimal.DecimalInputValidator;
import uk.co.nstauthority.fieldconsents.validation.ValidatorUtils;

@Component
public class ConsentProductionFiguresInputValidator implements Validator {

  public void validate(Object target, Errors errors) {
    var consentProductionFiguresInput = (ConsentProductionFiguresInput) target;

    var validator = DecimalInputValidator.builder()
        .mustBeMoreThanOrEqualTo(BigDecimal.ZERO)
        .mustHaveNoMoreThanDecimalPlaces(ValidatorUtils.MAX_DECIMAL_PLACES);

    validator.validate(consentProductionFiguresInput.getMinOilInput(), errors);
    validator.validate(consentProductionFiguresInput.getMaxOilInput(), errors);
    validator.validate(consentProductionFiguresInput.getMinGasInput(), errors);
    validator.validate(consentProductionFiguresInput.getMaxGasInput(), errors);

    // if no oil errors validate that the max is more than or equal to the min
    if (!errors.hasFieldErrors("minOilInput.inputValue")
        && !errors.hasFieldErrors("maxOilInput.inputValue")) {
      DecimalInputValidator.builder()
          .mustBeMoreThanOrEqualTo(
              consentProductionFiguresInput.getMinOilInput().getAsBigDecimal()
                  .orElseThrow(NoSuchElementException::new))
          .validate(consentProductionFiguresInput.getMaxOilInput(), errors);
    }

    // if no gas errors validate that the max is more than or equal to the min
    if (!errors.hasFieldErrors("minGasInput.inputValue")
        && !errors.hasFieldErrors("maxGasInput.inputValue")) {
      DecimalInputValidator.builder()
          .mustBeMoreThanOrEqualTo(
              consentProductionFiguresInput.getMinGasInput().getAsBigDecimal()
                  .orElseThrow(NoSuchElementException::new))
          .validate(consentProductionFiguresInput.getMaxGasInput(), errors);
    }
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(ConsentProductionFiguresInput.class);
  }
}
