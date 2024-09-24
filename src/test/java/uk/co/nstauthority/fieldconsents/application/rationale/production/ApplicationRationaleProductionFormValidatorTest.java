package uk.co.nstauthority.fieldconsents.application.rationale.production;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ApplicationRationaleProductionFormValidatorTest {

  @Mock
  private ApplicationRationaleFormValidatorHelper validatorHelper;

  @InjectMocks
  private ApplicationRationaleProductionFormValidator validator;

  @Test
  void validate_increase_withoutComment() {
    var hostLocationAssetKey = "hostKey";
    var nonHostLocationAssetKeys = List.of("first", "second", "third");
    var nonHostLocationAssetKeysSelectorField = "productionLocationAssetKeysSelector";

    var form = new ApplicationRationaleProductionForm(
        ApplicationRationaleType.INCREASE,
        null,
        null,
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
    var nonHostLocationAssetKeysSelectorField = "productionLocationAssetKeysSelector";

    var form = new ApplicationRationaleProductionForm(
        ApplicationRationaleType.DECREASE,
        null,
        null,
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

  @Test
  void validate_extension_withoutComment() {
    var hostLocationAssetKey = "hostKey";
    var nonHostLocationAssetKeys = List.of("first", "second", "third");
    var nonHostLocationAssetKeysSelectorField = "productionLocationAssetKeysSelector";

    var form = new ApplicationRationaleProductionForm(
        ApplicationRationaleType.EXTENSION,
        null,
        null,
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
            tuple("extensionComment.inputValue", "extensionComment.required", "Enter why you are requesting an extension")
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
  void validate_other_withoutComment() {
    var hostLocationAssetKey = "hostKey";
    var nonHostLocationAssetKeys = List.of("first", "second", "third");
    var nonHostLocationAssetKeysSelectorField = "productionLocationAssetKeysSelector";

    var form = new ApplicationRationaleProductionForm(
        ApplicationRationaleType.OTHER,
        null,
        null,
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
            tuple("otherComment.inputValue", "otherComment.required", "Enter why you have selected 'other'")
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

  private BindingResult getBindingResult(Object form) {
    return new BeanPropertyBindingResult(form, "form");
  }

}
