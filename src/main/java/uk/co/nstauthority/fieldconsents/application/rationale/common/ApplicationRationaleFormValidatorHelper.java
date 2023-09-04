package uk.co.nstauthority.fieldconsents.application.rationale.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Component
public class ApplicationRationaleFormValidatorHelper {

  private static final String HOST_LOCATION_ASSET_KEY = "hostLocationAssetKey";
  private static final String REQUIRED = "required";
  private static final String INVALID = "invalid";
  private static final String ASSET_VALIDATION_REQUEST_PURPOSE = "validating that assets have operators and licences";

  private final FieldService fieldService;
  private final TerminalService terminalService;

  ApplicationRationaleFormValidatorHelper(
      FieldService fieldService,
      TerminalService terminalService
  ) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  public void validateLocationAssets(List<String> locationAssetKeys, String field, Errors errors) {
    var assetKeys = locationAssetKeys.stream().map(AssetKey::parse).flatMap(Optional::stream).toList();

    if (locationAssetKeys.isEmpty()) {
      errors.rejectValue(field, REQUIRED, "Add at least one location");
      return;
    }

    if (assetKeys.size() != locationAssetKeys.size()) {
      errors.rejectValue(field, INVALID, "Invalid locations submitted");
      return;
    }

    if (new HashSet<>(assetKeys).size() != assetKeys.size()) {
      errors.rejectValue(field, INVALID, "Locations must be unique");
      return;
    }

    if (!assetsContainOperatorAndLicenses(assetKeys)) {
      errors.rejectValue(field, INVALID, "One or more locations don't have an operator or licenses");
    }
  }

  public void validateHostLocationAsset(String hostLocationAssetKey, List<AssetKey> locationAssetKeys, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, HOST_LOCATION_ASSET_KEY, REQUIRED, "You must provide a host location");

    if (errors.hasFieldErrors(HOST_LOCATION_ASSET_KEY)) {
      return;
    }

    var hostLocationAssetKeyOptional = AssetKey.parse(hostLocationAssetKey);
    if (hostLocationAssetKeyOptional.isEmpty()) {
      errors.rejectValue(HOST_LOCATION_ASSET_KEY, INVALID, "Invalid host location submitted");
      return;
    }

    if (locationAssetKeys.isEmpty()) {
      return;
    }

    var isLocationAsset = locationAssetKeys.contains(hostLocationAssetKeyOptional.get());
    if (!isLocationAsset) {
      errors.rejectValue(HOST_LOCATION_ASSET_KEY, INVALID, "The host location must be one of the above locations");
      return;
    }

    boolean containsOperatorAndLicenses = AssetKey.parse(hostLocationAssetKey)
        .map(assetKey -> assetsContainOperatorAndLicenses(Collections.singletonList(assetKey)))
        .orElse(false);

    if (!containsOperatorAndLicenses) {
      errors.rejectValue(HOST_LOCATION_ASSET_KEY, INVALID, "Select a location with an operator");
    }
  }

  private boolean assetsContainOperatorAndLicenses(List<AssetKey> assetKeys) {
    var fieldIds = assetKeys.stream()
        .filter(assetKey -> AssetType.FIELD.equals(assetKey.assetType()))
        .map(AssetKey::assetId)
        .toList();

    var allFieldsHaveOperatorsAndLicenses = fieldService
        .findFieldsWithOperatorAndLicences(fieldIds, ASSET_VALIDATION_REQUEST_PURPOSE)
        .stream()
        .allMatch(field -> field.operatorExists() && field.licencesExist());

    if (!allFieldsHaveOperatorsAndLicenses) {
      return false;
    }

    var terminalIds = assetKeys.stream()
        .filter(assetKey -> AssetType.TERMINAL.equals(assetKey.assetType()))
        .map(AssetKey::assetId)
        .toList();

    return terminalService
        .findTerminalsWithOperator(terminalIds, ASSET_VALIDATION_REQUEST_PURPOSE)
        .stream()
        .allMatch(AssetWithOperatorJson::operatorExists);
  }

}
