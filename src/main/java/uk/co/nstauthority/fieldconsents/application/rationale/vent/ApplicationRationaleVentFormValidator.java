package uk.co.nstauthority.fieldconsents.application.rationale.vent;

import static uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType.INCREASE;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.rationale.common.ApplicationRationaleFormValidatorHelper;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;

@Component
class ApplicationRationaleVentFormValidator implements Validator {

  private final ApplicationRationaleFormValidatorHelper validatorHelper;

  ApplicationRationaleVentFormValidator(ApplicationRationaleFormValidatorHelper validatorHelper) {
    this.validatorHelper = validatorHelper;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ApplicationRationaleVentForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        "rationaleType",
        "required",
        "Select whether this application is for an increase, decrease or no change"
    );

    var form = (ApplicationRationaleVentForm) target;

    if (INCREASE.equals(form.rationaleType())) {
      StringInputValidator.builder().validate(form.increaseComment(), errors);
    }

    validatorHelper.validateLocationAssets(
        form.ventingLocationAssetKeys(),
        "ventingLocationAssetKeysSelector",
        errors
    );

    validatorHelper.validateHostLocationAsset(
        form.hostLocationAssetKey(),
        form.ventingLocationAssetKeys().stream().map(AssetKey::parse).flatMap(Optional::stream).toList(),
        errors
    );
  }

}
