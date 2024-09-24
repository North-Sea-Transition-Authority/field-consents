package uk.co.nstauthority.fieldconsents.application.rationale.emissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.application.rationale.common.ApplicationRationaleFormValidatorHelper;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleFormValidatorTest {

  @Mock
  private ApplicationRationaleFormValidatorHelper validatorHelper;

  @InjectMocks
  private ApplicationRationaleFormValidator validator;

  @Test
  void validate_increase_withoutComment() {
    var hostLocationAssetKey = "hostKey";
    var nonHostLocationAssetKeys = List.of("first", "second", "third");
    var nonHostLocationAssetKeysSelectorField = "locationAssetKeysSelector";

    var form = new ApplicationRationaleForm(
        ApplicationRationaleType.INCREASE,
        null,
        null,
        nonHostLocationAssetKeysSelectorField,
        nonHostLocationAssetKeys,
        hostLocationAssetKey
    );
    var bindingResult = getBindingResult(form);

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("increaseComment.inputValue", "increaseComment.required", "Enter why you are requesting an increase")
        );

    verify(validatorHelper).validateLocationAssets(
        nonHostLocationAssetKeys,
        nonHostLocationAssetKeysSelectorField,
        bindingResult
    );

    verify(validatorHelper).validateHostLocationAsset(
        hostLocationAssetKey,
        nonHostLocationAssetKeys.stream().map(AssetKey::parse).flatMap(Optional::stream).toList(),
        bindingResult
    );
  }

  @Test
  void validate_decrease_withoutComment() {
    var hostLocationAssetKey = "hostKey";
    var nonHostLocationAssetKeys = List.of("first", "second", "third");
    var nonHostLocationAssetKeysSelectorField = "locationAssetKeysSelector";

    var form = new ApplicationRationaleForm(
        ApplicationRationaleType.DECREASE,
        null,
        null,
        nonHostLocationAssetKeysSelectorField,
        nonHostLocationAssetKeys,
        hostLocationAssetKey
    );
    var bindingResult = getBindingResult(form);

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors())
        .extracting(FieldError::getField, FieldError::getCode, FieldError::getDefaultMessage)
        .containsExactly(
            tuple("decreaseComment.inputValue", "decreaseComment.required", "Enter why you are requesting a decrease")
        );

    verify(validatorHelper).validateLocationAssets(
        nonHostLocationAssetKeys,
        nonHostLocationAssetKeysSelectorField,
        bindingResult
    );

    verify(validatorHelper).validateHostLocationAsset(
        hostLocationAssetKey,
        nonHostLocationAssetKeys.stream().map(AssetKey::parse).flatMap(Optional::stream).toList(),
        bindingResult
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationRationaleType.class, names = {"INCREASE", "DECREASE"}, mode = Mode.EXCLUDE)
  void validate_notIncrease_notDecrease_withoutComment(ApplicationRationaleType applicationRationaleType) {
    var hostLocationAssetKey = "hostKey";
    var nonHostLocationAssetKeys = List.of("first", "second", "third");
    var nonHostLocationAssetKeysSelectorField = "locationAssetKeysSelector";

    var form = new ApplicationRationaleForm(
        applicationRationaleType,
        null,
        null,
        nonHostLocationAssetKeysSelectorField,
        nonHostLocationAssetKeys,
        hostLocationAssetKey
    );
    var bindingResult = getBindingResult(form);

    validator.validate(form, bindingResult);

    assertThat(bindingResult.getFieldErrors()).isEmpty();

    verify(validatorHelper).validateLocationAssets(
        nonHostLocationAssetKeys,
        nonHostLocationAssetKeysSelectorField,
        bindingResult
    );

    verify(validatorHelper).validateHostLocationAsset(
        hostLocationAssetKey,
        nonHostLocationAssetKeys.stream().map(AssetKey::parse).flatMap(Optional::stream).toList(),
        bindingResult
    );
  }

  private BindingResult getBindingResult(Object form) {
    return new BeanPropertyBindingResult(form, "form");
  }

}
