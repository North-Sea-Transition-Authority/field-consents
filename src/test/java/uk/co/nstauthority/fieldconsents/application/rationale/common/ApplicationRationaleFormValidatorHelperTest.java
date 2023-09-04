package uk.co.nstauthority.fieldconsents.application.rationale.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNullOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithNullOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleFormValidatorHelperTest {

  private static final String NON_HOST_LOCATIONS_FIELD = "nonHostLocations";
  private static final String HOST_LOCATION_FIELD = "hostLocationAssetKey";

  private static final List<String> NON_HOST_LOCATIONS = List.of("1FIELD", "2TERMINAL");
  private static final List<AssetKey> NON_HOST_LOCATIONS_ASSET_KEYS = NON_HOST_LOCATIONS
      .stream()
      .map(AssetKey::parse)
      .flatMap(Optional::stream)
      .toList();

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @InjectMocks
  private ApplicationRationaleFormValidatorHelper validatorHelper;

  @Test
  void validateNonHostLocations_noneAdded() {
    var form = new Form(Collections.emptyList(), null);
    var bindingResult = getBindingResult(form);

    validatorHelper.validateLocationAssets(form.nonHostLocations(), NON_HOST_LOCATIONS_FIELD, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(NON_HOST_LOCATIONS_FIELD, "required", "Add at least one location")
        );
  }

  @Test
  void validateNonHostLocations_invalidAssetKeys() {
    var flaringLocations = List.of("1Terminal", "2FIELD", "99???");
    var form = new Form(flaringLocations, null);
    var bindingResult = getBindingResult(form);

    validatorHelper.validateLocationAssets(form.nonHostLocations(), NON_HOST_LOCATIONS_FIELD, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(NON_HOST_LOCATIONS_FIELD, "invalid", "Invalid locations submitted")
        );
  }

  @Test
  void validateNonHostLocations_nonUniqueAssetKeys() {
    var flaringLocations = List.of("1FIELD", "1TERMINAL", "1FIELD");
    var form = new Form(flaringLocations, null);
    var bindingResult = getBindingResult(form);

    validatorHelper.validateLocationAssets(form.nonHostLocations(), NON_HOST_LOCATIONS_FIELD, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(NON_HOST_LOCATIONS_FIELD, "invalid", "Locations must be unique")
        );
  }

  @Test
  void validateNonHostLocations_terminalsDontHaveOperator() {
    var form = new Form(NON_HOST_LOCATIONS, null);
    var bindingResult = getBindingResult(form);

    when(fieldService.findFieldsWithOperatorAndLicences(eq(Collections.singletonList(1)), anyString()))
        .thenReturn(Collections.singletonList(field1JsonWithOperatorAndLicences));

    when(terminalService.findTerminalsWithOperator(eq(Collections.singletonList(2)), anyString()))
        .thenReturn(Collections.singletonList(terminal1JsonWithNullOperator));

    validatorHelper.validateLocationAssets(form.nonHostLocations(), NON_HOST_LOCATIONS_FIELD, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(NON_HOST_LOCATIONS_FIELD, "invalid", "One or more locations don't have an operator or licenses")
        );
  }

  @Test
  void validateNonHostLocations_fieldsDontHaveOperatorOrLicenses() {
    var form = new Form(NON_HOST_LOCATIONS, null);
    var bindingResult = getBindingResult(form);

    when(fieldService.findFieldsWithOperatorAndLicences(eq(Collections.singletonList(1)), anyString()))
        .thenReturn(Collections.singletonList(field1JsonWithNullOperatorAndLicences));

    validatorHelper.validateLocationAssets(form.nonHostLocations(), NON_HOST_LOCATIONS_FIELD, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(NON_HOST_LOCATIONS_FIELD, "invalid", "One or more locations don't have an operator or licenses")
        );
  }

  @Test
  void validateNonHostLocations() {
    var form = new Form(NON_HOST_LOCATIONS, null);
    var bindingResult = getBindingResult(form);

    when(fieldService.findFieldsWithOperatorAndLicences(eq(Collections.singletonList(1)), anyString()))
        .thenReturn(Collections.singletonList(field1JsonWithOperatorAndLicences));

    when(terminalService.findTerminalsWithOperator(eq(Collections.singletonList(2)), anyString()))
        .thenReturn(Collections.singletonList(terminal1JsonWithOperator));

    validatorHelper.validateLocationAssets(form.nonHostLocations(), NON_HOST_LOCATIONS_FIELD, bindingResult);

    assertThat(bindingResult.getFieldErrors()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {""})
  @NullSource
  void validateHostLocation_null(String hostLocationAssetKey) {
    var form = new Form(Collections.emptyList(), hostLocationAssetKey);
    var bindingResult = getBindingResult(form);

    validatorHelper.validateHostLocationAsset(form.hostLocationAssetKey(), NON_HOST_LOCATIONS_ASSET_KEYS, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(HOST_LOCATION_FIELD, "required", "You must provide a host location")
        );
  }

  @Test
  void validateHostLocation_invalidAssetId() {
    var hostLocationAssetKey = "a";
    var form = new Form(Collections.emptyList(), hostLocationAssetKey);
    var bindingResult = getBindingResult(form);

    validatorHelper.validateHostLocationAsset(form.hostLocationAssetKey(), NON_HOST_LOCATIONS_ASSET_KEYS, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(HOST_LOCATION_FIELD, "invalid", "Invalid host location submitted")
        );
  }

  @Test
  void validateHostLocation_notFlaringLocation() {
    var hostLocationAssetKey = "99FIELD";
    var form = new Form(NON_HOST_LOCATIONS, hostLocationAssetKey);
    var bindingResult = getBindingResult(form);

    validatorHelper.validateHostLocationAsset(form.hostLocationAssetKey(), NON_HOST_LOCATIONS_ASSET_KEYS, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(HOST_LOCATION_FIELD, "invalid", "The host location must be one of the above locations")
        );
  }

  @Test
  void validateHostLocation_doesNotHaveOperator() {
    var hostLocationAssetKey = NON_HOST_LOCATIONS.get(1);
    var form = new Form(NON_HOST_LOCATIONS, hostLocationAssetKey);
    var bindingResult = getBindingResult(form);

    when(terminalService.findTerminalsWithOperator(eq(Collections.singletonList(2)), anyString()))
        .thenReturn(Collections.singletonList(terminal1JsonWithNullOperator));

    validatorHelper.validateHostLocationAsset(form.hostLocationAssetKey(), NON_HOST_LOCATIONS_ASSET_KEYS, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple(HOST_LOCATION_FIELD, "invalid", "Select a location with an operator")
        );
  }

  private record Form(List<String> nonHostLocations, String hostLocationAssetKey) {}

  private BindingResult getBindingResult(Object form) {
    return new BeanPropertyBindingResult(form, "form");
  }

}
