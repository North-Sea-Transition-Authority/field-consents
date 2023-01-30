package uk.co.nstauthority.fieldconsents.application.assets;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;

@Service
class AdditionalAssetSelectionFormValidator implements Validator {

  static final String ASSET_KEY_FIELD_NAME = "assetKey";

  public static final String ASSET_EMPTY = "You must select a field to add";

  public static final String ASSET_MUST_HAVE_OPERATOR_LICENCES =
      "This field does not have an operator or any associated licences therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_OPERATOR =
      "This field does not have an operator therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_LICENCES =
      "This field does not have any associated licences therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL =
      " please contact the %s if you think the field should have this information";

  public static final String ASSET_MUST_BE_FIELD = "You must select a field";

  private final AssetService assetService;

  private final FieldService fieldService;

  private final CustomerConfigurationProperties customerConfigurationProperties;

  @Autowired
  AdditionalAssetSelectionFormValidator(AssetService assetService,
                                        FieldService fieldService,
                                        CustomerConfigurationProperties customerConfigurationProperties) {
    this.assetService = assetService;
    this.fieldService = fieldService;
    this.customerConfigurationProperties = customerConfigurationProperties;
  }

  @Override
  public boolean supports(@NotNull Class<?> clazz) {
    return AssetSelectionForm.class.equals(clazz);
  }

  @Override
  public void validate(@NotNull Object target, @NotNull Errors errors) {
    var form = (AssetSelectionForm) target;
    var purpose = "Check operator and associated licences exist when adding a field to an application";

    ValidationUtils.rejectIfEmpty(errors, ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".required",
        ASSET_EMPTY);

    if (!errors.hasErrors()) {
      AssetJson assetJson = assetService.getAsset(form.getAssetKey());

      if (assetJson.getAssetType() == AssetType.FIELD) {
        FieldWithOperatorAndLicencesJson fieldJson =
            fieldService.getFieldWithOperatorAndLicences(assetJson.getId(), purpose);

        if (!fieldJson.operatorExists() && !fieldJson.licencesExist()) {
          errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveOperatorAndLicences",
              ASSET_MUST_HAVE_OPERATOR_LICENCES +
              ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerConfigurationProperties.mnemonic()));
        } else if (!fieldJson.operatorExists()) {
          errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveOperator",
              ASSET_MUST_HAVE_OPERATOR +
              ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerConfigurationProperties.mnemonic()));
        } else if (!fieldJson.licencesExist()) {
          errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveLicences",
              ASSET_MUST_HAVE_LICENCES +
              ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerConfigurationProperties.mnemonic()));
        }
      } else if (assetJson.getAssetType() == AssetType.TERMINAL) {
        errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustBeAField",
            ASSET_MUST_BE_FIELD);
      }
    }
  }
}
