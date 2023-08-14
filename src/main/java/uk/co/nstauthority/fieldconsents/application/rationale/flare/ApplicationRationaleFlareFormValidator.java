package uk.co.nstauthority.fieldconsents.application.rationale.flare;

import static uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType.INCREASE;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import uk.co.fivium.formlibrary.validator.string.StringInputValidator;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Component
class ApplicationRationaleFlareFormValidator implements Validator {

  private static final String REQUIRED = "required";
  private static final String INVALID = "invalid";
  private static final String ASSET_VALIDATION_REQUEST_PURPOSE = "validating that assets have operators and licences";
  private static final String FLARING_LOCATIONS_SELECTOR_FIELD = "flaringLocationAssetKeysSelector";
  private static final String HOST_LOCATION_FIELD = "hostLocationAssetKey";

  private final FieldService fieldService;
  private final TerminalService terminalService;

  ApplicationRationaleFlareFormValidator(
      FieldService fieldService,
      TerminalService terminalService
  ) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return ApplicationRationaleFlareForm.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        "rationaleType",
        REQUIRED,
        "Select whether this application is for an increase, decrease or no change"
    );

    var form = (ApplicationRationaleFlareForm) target;

    if (INCREASE.equals(form.rationaleType())) {
      StringInputValidator.builder().validate(form.increaseComment(), errors);
    }

    validateFlaringLocations(form, errors);
    validateHostLocation(form, errors);
  }

  void validateFlaringLocations(ApplicationRationaleFlareForm form, Errors errors) {
    if (form.flaringLocationAssetKeys().isEmpty()) {
      errors.rejectValue(
          FLARING_LOCATIONS_SELECTOR_FIELD,
          REQUIRED,
          "Add at least one flaring location"
      );
      return;
    }

    var assetKeysAreValid = form.flaringLocationAssetKeys().stream().map(AssetKey::parse).allMatch(Optional::isPresent);
    if (!assetKeysAreValid) {
      errors.rejectValue(
          FLARING_LOCATIONS_SELECTOR_FIELD,
          INVALID,
          "Invalid flaring locations submitted"
      );
      return;
    }

    if (!areFlaringLocationsUnique(form.flaringLocationAssetKeys())) {
      errors.rejectValue(
          FLARING_LOCATIONS_SELECTOR_FIELD,
          INVALID,
          "Flaring locations must be unique"
      );
      return;
    }

    if (!assetsContainOperatorAndLicenses(form.flaringLocationAssetKeys())) {
      errors.rejectValue(
          FLARING_LOCATIONS_SELECTOR_FIELD,
          INVALID,
          "One or more locations don't have an operator or licenses"
      );
    }
  }

  void validateHostLocation(ApplicationRationaleFlareForm form, Errors errors) {
    ValidationUtils.rejectIfEmpty(
        errors,
        HOST_LOCATION_FIELD,
        REQUIRED,
        "You must provide a host location"
    );

    if (errors.hasFieldErrors(HOST_LOCATION_FIELD)) {
      return;
    }

    if (AssetKey.parse(form.hostLocationAssetKey()).isEmpty()) {
      errors.rejectValue(
          HOST_LOCATION_FIELD,
          INVALID,
          "Invalid host location submitted"
      );
      return;
    }

    if (!form.flaringLocationAssetKeys().contains(form.hostLocationAssetKey())) {
      errors.rejectValue(
          HOST_LOCATION_FIELD,
          INVALID,
          "The host location must be one of the flaring locations"
      );
      return;
    }

    if (!assetsContainOperatorAndLicenses(Collections.singletonList(form.hostLocationAssetKey()))) {
      errors.rejectValue(
          HOST_LOCATION_FIELD,
          INVALID,
          "Select a location with an operator"
      );
    }
  }

  private boolean areFlaringLocationsUnique(List<String> assetKeys) {
    var uniqueKeys = new HashSet<>(assetKeys);
    return uniqueKeys.size() == assetKeys.size();
  }

  private boolean assetsContainOperatorAndLicenses(List<String> assetKeys) {
    var fieldIds = assetKeys.stream()
        .map(AssetKey::parse)
        .flatMap(Optional::stream)
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
        .map(AssetKey::parse)
        .flatMap(Optional::stream)
        .filter(assetKey -> AssetType.TERMINAL.equals(assetKey.assetType()))
        .map(AssetKey::assetId)
        .toList();

    return terminalService
        .findTerminalsWithOperator(terminalIds, ASSET_VALIDATION_REQUEST_PURPOSE)
        .stream()
        .allMatch(AssetWithOperatorJson::operatorExists);
  }

}
