package uk.co.nstauthority.fieldconsents.application.assets;

import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE;

import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties;

@Service
class AdditionalAssetSelectionFormValidator {

  static final String ASSET_KEY_FIELD_NAME = "assetKey";

  public static final String ASSET_EMPTY = "Select a field";

  public static final String ASSET_MUST_HAVE_OPERATOR_LICENCES =
      "%s does not have an operator or any associated licences therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_OPERATOR =
      "%s does not have an operator therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_LICENCES =
      "%s does not have any associated licences therefore cannot be added to this application, ";

  public static final String ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL =
      " please contact %s if you think the field should have this information";

  public static final String ASSET_MUST_BE_FIELD = "You must select a field";

  public static final String DUPLICATED_PRIMARY_FIELD = "%s is the primary field of this application";

  public static final String DUPLICATED_SECONDARY_FIELD = "%s has already been added as an additional field on this application";

  public static final String ASSET_MUST_HAVE_ALLOWED_STATUS = "%s " + FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE;

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

  public void validate(AssetSelectionForm form, @NotNull Errors errors, ApplicationVersion applicationVersion) {
    var purpose = "Check field status and that an operator and associated licences exist when adding a field to an application";

    var assetJson = form.getAssetKey().flatMap(assetService::findAsset).orElse(null);
    if (assetJson == null) {
      errors.rejectValue(ASSET_KEY_FIELD_NAME, "empty", ASSET_EMPTY);
      return;
    }

    if (assetJson.getAssetType() != AssetType.FIELD) {
      errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustBeAField",
          ASSET_MUST_BE_FIELD);
      return;
    }

    FieldWithOperatorAndLicencesJson fieldJson =
        fieldService.getFieldWithOperatorAndLicences(assetJson.getId(), purpose);

    rejectIfDuplicatedField(errors, applicationVersion, fieldJson);

    if (errors.hasFieldErrors(ASSET_KEY_FIELD_NAME)) {
      return;
    }

    if (!FIELD_STATUSES_ALLOWED.contains(fieldJson.getStatusJson().status())) {
      errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveAllowedStatus",
          ASSET_MUST_HAVE_ALLOWED_STATUS.formatted(fieldJson.getName()));
      return;
    }

    if (!fieldJson.operatorExists() && !fieldJson.licencesExist()) {
      errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveOperatorAndLicences",
          ASSET_MUST_HAVE_OPERATOR_LICENCES.formatted(fieldJson.getName()) +
          ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerBrandingConfigurationProperties.email()));
    } else if (!fieldJson.operatorExists()) {
      errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveOperator",
          ASSET_MUST_HAVE_OPERATOR.formatted(fieldJson.getName()) +
          ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerBrandingConfigurationProperties.email()));
    } else if (!fieldJson.licencesExist()) {
      errors.rejectValue(ASSET_KEY_FIELD_NAME, ASSET_KEY_FIELD_NAME + ".assetMustHaveLicences",
          ASSET_MUST_HAVE_LICENCES.formatted(fieldJson.getName()) +
          ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL.formatted(customerBrandingConfigurationProperties.email()));
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
