package uk.co.nstauthority.fieldconsents.application.rationale.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNullOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithNullOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleFlareFormValidatorTest {

  private static final List<String> FLARING_LOCATIONS = List.of("1FIELD", "2TERMINAL");

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @Spy
  @InjectMocks
  private ApplicationRationaleFlareFormValidator validator;

  @Test
  void supports() {
    assertThat(validator.supports(ApplicationRationaleFlareForm.class)).isTrue();
  }

  @Test
  void validate_increase_withoutComment() {
    var form = new ApplicationRationaleFlareForm(ApplicationRationaleType.INCREASE, null, null, Collections.emptyList(),
        null);
    var bindingResult = getBindingResult(form);

    doNothing().when(validator).validateFlaringLocations(form, bindingResult);
    doNothing().when(validator).validateHostLocation(form, bindingResult);

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("increaseComment.inputValue", "increaseComment.required", "Enter why you are requesting an increase")
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationRationaleType.class, names = "INCREASE", mode = Mode.EXCLUDE)
  void validate_notIncrease_withoutComment(ApplicationRationaleType applicationRationaleType) {
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, Collections.emptyList(), null);
    var bindingResult = getBindingResult(form);

    doNothing().when(validator).validateFlaringLocations(form, bindingResult);
    doNothing().when(validator).validateHostLocation(form, bindingResult);

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void validateFlaringLocations_nonAdded(ApplicationRationaleType applicationRationaleType) {
    List<String> flaringLocations = Collections.emptyList();
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, flaringLocations, null);
    var bindingResult = getBindingResult(form);

    validator.validateFlaringLocations(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("flaringLocationAssetKeysSelector", "required", "Add at least one flaring location")
        );
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void validateFlaringLocations_invalidAssetKeys(ApplicationRationaleType applicationRationaleType) {
    var flaringLocations = List.of("1Terminal", "2FIELD", "99???");
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, flaringLocations, null);
    var bindingResult = getBindingResult(form);

    validator.validateFlaringLocations(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("flaringLocationAssetKeysSelector", "invalid", "Invalid flaring locations submitted")
        );
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void validateFlaringLocations_nonUniqueAssetKeys(ApplicationRationaleType applicationRationaleType) {
    var flaringLocations = List.of("1FIELD", "1TERMINAL", "1FIELD");
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, flaringLocations, null);
    var bindingResult = getBindingResult(form);

    validator.validateFlaringLocations(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("flaringLocationAssetKeysSelector", "invalid", "Flaring locations must be unique")
        );
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void validateFlaringLocations_terminalsDontHaveOperator(ApplicationRationaleType applicationRationaleType) {
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, FLARING_LOCATIONS, null);
    var bindingResult = getBindingResult(form);

    when(fieldService.findFieldsWithOperatorAndLicences(eq(Collections.singletonList(1)), anyString()))
        .thenReturn(Collections.singletonList(field1JsonWithOperatorAndLicences));

    when(terminalService.findTerminalsWithOperator(eq(Collections.singletonList(2)), anyString()))
        .thenReturn(Collections.singletonList(terminal1JsonWithNullOperator));

    validator.validateFlaringLocations(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("flaringLocationAssetKeysSelector", "invalid",
                "One or more locations don't have an operator or licenses")
        );
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void validateFlaringLocations_fieldsDontHaveOperatorOrLicenses(ApplicationRationaleType applicationRationaleType) {
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, FLARING_LOCATIONS, null);
    var bindingResult = getBindingResult(form);

    when(fieldService.findFieldsWithOperatorAndLicences(eq(Collections.singletonList(1)), anyString()))
        .thenReturn(Collections.singletonList(field1JsonWithNullOperatorAndLicences));

    validator.validateFlaringLocations(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("flaringLocationAssetKeysSelector", "invalid",
                "One or more locations don't have an operator or licenses")
        );
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void validateFlaringLocations(ApplicationRationaleType applicationRationaleType) {
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, FLARING_LOCATIONS, null);
    var bindingResult = getBindingResult(form);

    when(fieldService.findFieldsWithOperatorAndLicences(eq(Collections.singletonList(1)), anyString()))
        .thenReturn(Collections.singletonList(field1JsonWithOperatorAndLicences));

    when(terminalService.findTerminalsWithOperator(eq(Collections.singletonList(2)), anyString()))
        .thenReturn(Collections.singletonList(terminal1JsonWithOperator));

    validator.validateFlaringLocations(form, bindingResult);

    assertThat(bindingResult.getFieldErrors()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {""})
  @NullSource
  void validateHostLocation_null(String hostLocationAssetKey) {
    var form = new ApplicationRationaleFlareForm(ApplicationRationaleType.INCREASE, null, null, Collections.emptyList(), hostLocationAssetKey);
    var bindingResult = getBindingResult(form);

    validator.validateHostLocation(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("hostLocationAssetKey", "required", "You must provide a host location")
        );
  }

  @ParameterizedTest
  @EnumSource
  void validateHostLocation_invalidAssetId(ApplicationRationaleType applicationRationaleType) {
    var hostLocationAssetKey = "a";
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, Collections.emptyList(), hostLocationAssetKey);
    var bindingResult = getBindingResult(form);

    validator.validateHostLocation(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("hostLocationAssetKey", "invalid", "Invalid host location submitted")
        );
  }

  @ParameterizedTest
  @EnumSource
  void validateHostLocation_notFlaringLocation(ApplicationRationaleType applicationRationaleType) {
    var hostLocationAssetKey = "99FIELD";
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, FLARING_LOCATIONS, hostLocationAssetKey);
    var bindingResult = getBindingResult(form);

    validator.validateHostLocation(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("hostLocationAssetKey", "invalid", "The host location must be one of the flaring locations")
        );
  }

  @ParameterizedTest
  @EnumSource
  void validateHostLocation_doesNotHaveOperator(ApplicationRationaleType applicationRationaleType) {
    var hostLocationAssetKey = FLARING_LOCATIONS.get(1);
    var form = new ApplicationRationaleFlareForm(applicationRationaleType, null, null, FLARING_LOCATIONS, hostLocationAssetKey);
    var bindingResult = getBindingResult(form);

    when(terminalService.findTerminalsWithOperator(eq(Collections.singletonList(2)), anyString()))
        .thenReturn(Collections.singletonList(terminal1JsonWithNullOperator));

    validator.validateHostLocation(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("hostLocationAssetKey", "invalid", "Select a location with an operator")
        );
  }

  private BindingResult getBindingResult(Object form) {
    return new BeanPropertyBindingResult(form, "form");
  }

}
