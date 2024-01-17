package uk.co.nstauthority.fieldconsents.application.assets;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;

@Service
class AdditionalAssetSelectionFormValidator implements Validator {

  static final String ASSET_KEY_FIELD_NAME = "assetKey";

  public static final String ASSET_EMPTY = "Select a field";

  public static final String ASSET_MUST_HAVE_OPERATOR_LICENCES =
      "This field does not have an operator or any associated licences therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_OPERATOR =
      "This field does not have an operator therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_LICENCES =
      "This field does not have any associated licences therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL =
      " please contact %s if you think the field should have this information";

  public static final String ASSET_MUST_BE_FIELD = "You must select a field";

  public static final String DUPLICATED_PRIMARY_FIELD = "%s is the primary field of this application";

  public static final String DUPLICATED_SECONDARY_FIELD = "%s has already been added as an additional field on this application";

  private final AssetService assetService;

  private final FieldService fieldService;

  private final CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties;

  private final ApplicationAssetService applicationAssetService;

  @Autowired
  AdditionalAssetSelectionFormValidator(AssetService assetService,
                                        FieldService fieldService,
                                        CustomerBrandingConfigurationProperties customerBrandingConfigurationProperties,
                                        ApplicationAssetService applicationAssetService) {
    this.assetService = assetService;
    this.fieldService = fieldService;
    this.customerBrandingConfigurationProperties = customerBrandingConfigurationProperties;
    this.applicationAssetService = applicationAssetService;
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

        rejectIfDuplicatedField(errors, form.getApplicationVersion(), fieldJson);
        if (!fieldJson.operatorExists() && !fieldJson.licencesExist()) {
          errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveOperatorAndLicences",
              ASSET_MUST_HAVE_OPERATOR_LICENCES +
              ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerBrandingConfigurationProperties.email()));
        } else if (!fieldJson.operatorExists()) {
          errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveOperator",
              ASSET_MUST_HAVE_OPERATOR +
              ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerBrandingConfigurationProperties.email()));
        } else if (!fieldJson.licencesExist()) {
          errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveLicences",
              ASSET_MUST_HAVE_LICENCES +
              ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerBrandingConfigurationProperties.email()));
        }
      } else if (assetJson.getAssetType() == AssetType.TERMINAL) {
        errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustBeAField",
            ASSET_MUST_BE_FIELD);
      }
    }
  }

  private void rejectIfDuplicatedField(@NotNull Errors errors, ApplicationVersion applicationVersion, FieldJson fieldJson) {
    var duplicatedApplicationAsset = applicationAssetService
        .findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY))
        .stream()
        .filter(applicationAsset -> AssetType.FIELD.equals(applicationAsset.getAssetType()))
        .filter(applicationAsset -> applicationAsset.getAssetId().equals(fieldJson.getId()))
        .findFirst();

    if (duplicatedApplicationAsset.isPresent()) {
      var applicationAsset = duplicatedApplicationAsset.get();
      if (applicationAsset.getAssetRole().equals(AssetRole.PRIMARY)) {
        errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".duplicated",
            DUPLICATED_PRIMARY_FIELD.formatted(fieldJson.getName()));
      } else {
        errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".duplicated",
            DUPLICATED_SECONDARY_FIELD.formatted(fieldJson.getName()));
      }
    }
  }
}
