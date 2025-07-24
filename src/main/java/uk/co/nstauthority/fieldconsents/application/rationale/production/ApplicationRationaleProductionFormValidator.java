package uk.co.nstauthority.fieldconsents.application.rationale.production;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.rationale.common.ApplicationRationaleFormValidatorHelper;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;

@Component
public class ApplicationRationaleProductionFormValidator {

  private static final String RATIONALE_TYPE_FIELD = "rationaleType";

  private final ApplicationRationaleFormValidatorHelper validatorHelper;

  ApplicationRationaleProductionFormValidator(ApplicationRationaleFormValidatorHelper validatorHelper) {
    this.validatorHelper = validatorHelper;
  }

  public void validate(ApplicationRationaleProductionForm form, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        RATIONALE_TYPE_FIELD,
        "required",
        "Select whether this application is for an increase, decrease, extension or other"
    );

    var rationaleType = form.rationaleType();
    if (rationaleType != null) {
      switch (rationaleType) {
        case INCREASE:
          StringInputValidator.builder().validate(form.increaseComment(), errors);
          break;
        case DECREASE:
          StringInputValidator.builder().validate(form.decreaseComment(), errors);
          break;
        case EXTENSION:
          StringInputValidator.builder().validate(form.extensionComment(), errors);
          break;
        case OTHER:
          StringInputValidator.builder().validate(form.otherComment(), errors);
          break;
        default:
          break;
      }
    }

    validatorHelper.validateLocationAssets(
        form.productionLocationAssetKeys(),
        "productionLocationAssetKeysSelector",
        errors
    );

    validatorHelper.validateHostLocationAsset(
        form.hostLocationAssetKey(),
        form.productionLocationAssetKeys().stream().map(AssetKey::parse).flatMap(Optional::stream).toList(),
        errors
    );
  }

}
