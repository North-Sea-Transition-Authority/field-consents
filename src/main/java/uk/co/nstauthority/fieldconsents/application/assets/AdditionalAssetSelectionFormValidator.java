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
import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;

@Service
class AdditionalAssetSelectionFormValidator implements Validator {

  static final String ASSET_KEY_FIELD_NAME = "assetKey";

  public static final String ASSET_EMPTY = "You must select a field to add";

  public static final String ASSET_MUST_HAVE_LICENCES =
      "This field does not have any associated licences therefore cannot be added to this application, " +
          "please contact %s if you think this field should have associated to licences";

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
    var purpose = "Check associated licences exist when adding a field to an application";

    ValidationUtils.rejectIfEmpty(errors, ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".required",
        ASSET_EMPTY);

    if (!errors.hasErrors()) {
      AssetJson assetJson = assetService.getAsset(form.getAssetKey());

      if (assetJson.getAssetType() == AssetType.FIELD
          && fieldService.getFieldWithOperatorAndLicences(assetJson.getId(), purpose).getLicences().isEmpty()
      ) {
        errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveLicences",
            ASSET_MUST_HAVE_LICENCES.formatted(customerConfigurationProperties.mnemonic()));
      } else if (assetJson.getAssetType() == AssetType.TERMINAL) {
        errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustBeAField",
            ASSET_MUST_BE_FIELD);
      }
    }
  }

}
