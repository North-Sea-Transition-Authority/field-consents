package uk.co.nstauthority.fieldconsents.application.rationale.common;

import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE;

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
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Component
public class ApplicationRationaleFormValidatorHelper {

  private static final String HOST_LOCATION_ASSET_KEY = "hostLocationAssetKey";
  private static final String REQUIRED = "required";
  private static final String INVALID = "invalid";
  private static final String ASSET_VALIDATION_REQUEST_PURPOSE = "validating that assets have correct state";

  private final FieldService fieldService;
  private final TerminalService terminalService;

  ApplicationRationaleFormValidatorHelper(
      FieldService fieldService,
      TerminalService terminalService
  ) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  public void validateLocationAssets(List<String> locationAssetKeys, String formField, Errors errors) {
    var assetKeys = locationAssetKeys.stream().map(AssetKey::parse).flatMap(Optional::stream).toList();

    if (locationAssetKeys.isEmpty()) {
      errors.rejectValue(formField, REQUIRED, "Add at least one location");
      return;
    }

    if (assetKeys.size() != locationAssetKeys.size()) {
      errors.rejectValue(formField, INVALID, "Invalid locations submitted");
      return;
    }

    if (new HashSet<>(assetKeys).size() != assetKeys.size()) {
      errors.rejectValue(formField, INVALID, "Locations must be unique");
      return;
    }

    var fields = getFieldsFromAssetKeys(assetKeys);

    addFieldStatusErrors(fields, formField, errors);

    if (errors.hasFieldErrors(formField)) {
      return;
    }

    if (!assetsContainOperatorAndLicenses(fields, assetKeys)) {
      errors.rejectValue(formField, INVALID, "One or more locations don't have an operator or licenses");
    }
  }

  public void validateHostLocationAsset(String hostLocationAssetKeyStr, List<AssetKey> locationAssetKeys, Errors errors) {
    ValidationUtils.rejectIfEmpty(errors, HOST_LOCATION_ASSET_KEY, REQUIRED, "You must provide a host location");

    if (errors.hasFieldErrors(HOST_LOCATION_ASSET_KEY)) {
      return;
    }

    var hostLocationAssetKeyOptional = AssetKey.parse(hostLocationAssetKeyStr);
    if (hostLocationAssetKeyOptional.isEmpty()) {
      errors.rejectValue(HOST_LOCATION_ASSET_KEY, INVALID, "Invalid host location submitted");
      return;
    }

    if (locationAssetKeys.isEmpty()) {
      return;
    }

    var hostLocationAssetKey = hostLocationAssetKeyOptional.get();
    var isLocationAsset = locationAssetKeys.contains(hostLocationAssetKey);
    if (!isLocationAsset) {
      errors.rejectValue(HOST_LOCATION_ASSET_KEY, INVALID, "The host location must be one of the above locations");
      return;
    }

    var fields = getFieldsFromAssetKeys(Collections.singletonList(hostLocationAssetKey));

    addFieldStatusErrors(fields, HOST_LOCATION_ASSET_KEY, errors);

    if (errors.hasFieldErrors(HOST_LOCATION_ASSET_KEY)) {
      return;
    }

    if (!assetsContainOperatorAndLicenses(fields, Collections.singletonList(hostLocationAssetKey))) {
      errors.rejectValue(HOST_LOCATION_ASSET_KEY, INVALID, "Select a location with an operator");
    }
  }

  private List<FieldWithOperatorAndLicencesJson> getFieldsFromAssetKeys(List<AssetKey> assetKeys) {
    var fieldIds = assetKeys.stream()
        .filter(assetKey -> AssetType.FIELD.equals(assetKey.assetType()))
        .map(AssetKey::assetId)
        .toList();

    return fieldService.findFieldsWithOperatorAndLicences(fieldIds, ASSET_VALIDATION_REQUEST_PURPOSE);
  }

  private boolean assetsContainOperatorAndLicenses(
      List<FieldWithOperatorAndLicencesJson> fields,
      List<AssetKey> assetKeys
  ) {
    var allFieldsHaveOperatorsAndLicenses = fields
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

  private void addFieldStatusErrors(List<? extends FieldJson> fields, String formField, Errors errors) {
    fields
        .stream()
        .filter(field -> !FIELD_STATUSES_ALLOWED.contains(field.getStatusJson().status()))
        .forEach(field ->
            errors.rejectValue(formField, INVALID,
                "%s %s".formatted(field.getName(), FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE))
        );
  }
}
