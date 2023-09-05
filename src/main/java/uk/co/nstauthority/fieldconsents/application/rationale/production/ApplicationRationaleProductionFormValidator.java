package uk.co.nstauthority.fieldconsents.application.rationale.production;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.application.rationale.common.ApplicationRationaleFormValidatorHelper;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;

@Component
class ApplicationRationaleProductionFormValidator implements Validator {

  private static final String RATIONALE_TYPE_FIELD = "rationaleType";

  private final ApplicationRationaleFormValidatorHelper validatorHelper;

  ApplicationRationaleProductionFormValidator(ApplicationRationaleFormValidatorHelper validatorHelper) {
    this.validatorHelper = validatorHelper;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ApplicationRationaleProductionForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        RATIONALE_TYPE_FIELD,
        "required",
        "Select whether this application is for an increase, decrease, extension or other"
    );

    var form = (ApplicationRationaleProductionForm) target;

    var rationaleType = form.rationaleType();
    if (ApplicationRationaleType.EXTENSION.equals(rationaleType)) {
      StringInputValidator.builder().validate(form.extensionComment(), errors);
    }
    if (ApplicationRationaleType.OTHER.equals(rationaleType)) {
      StringInputValidator.builder().validate(form.otherComment(), errors);
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
