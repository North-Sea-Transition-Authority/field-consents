package uk.co.nstauthority.fieldconsents.application.rationale.emissions;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.rationale.common.ApplicationRationaleFormValidatorHelper;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;

@Component
public class ApplicationRationaleFormValidator {

  private final ApplicationRationaleFormValidatorHelper validatorHelper;

  ApplicationRationaleFormValidator(ApplicationRationaleFormValidatorHelper validatorHelper) {
    this.validatorHelper = validatorHelper;
  }

  public void validate(ApplicationRationaleForm form, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        "rationaleType",
        "required",
        "Select whether this application is for an increase, decrease or no change"
    );

    var rationaleType = form.rationaleType();
    switch (rationaleType) {
      case INCREASE:
        StringInputValidator.builder().validate(form.increaseComment(), errors);
        break;
      case DECREASE:
        StringInputValidator.builder().validate(form.decreaseComment(), errors);
        break;
      case NO_CHANGE:
      default:
        break;
    }

    validatorHelper.validateLocationAssets(
        form.locationAssetKeys(),
        "locationAssetKeysSelector",
        errors
    );

    validatorHelper.validateHostLocationAsset(
        form.hostLocationAssetKey(),
        form.locationAssetKeys().stream().map(AssetKey::parse).flatMap(Optional::stream).toList(),
        errors
    );
  }

}
