package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetTestUtil;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class AdditionalAssetSelectionFormValidatorTest {

  @Mock
  private AssetService assetService;

  @Mock
  private FieldService fieldService;

  private final CustomerConfigurationProperties customerConfigurationProperties
      = ValidatorTestingUtil.getCustomerConfigurationProperties();

  private AdditionalAssetSelectionFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  private AssetSelectionForm form;

  @BeforeEach
  void setUp() {
    validator = new AdditionalAssetSelectionFormValidator(assetService, fieldService,
        customerConfigurationProperties);
    form = new AssetSelectionForm();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void validate_emptyForm() {
    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(AdditionalAssetSelectionFormValidator.ASSET_EMPTY))
    );
  }

  @Test
  void validate_terminalAsset() {
    form.setAssetKey(AssetTestUtil.TERMINAL1_ASSET_KEY);

    when(assetService.getAsset(AssetTestUtil.TERMINAL1_ASSET_KEY))
        .thenReturn(AssetTestUtil.terminal1AssetJson);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);

    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(AdditionalAssetSelectionFormValidator.ASSET_MUST_BE_FIELD))
    );
  }

  @Test
  void validate_fieldAssetNoLicences() {
    form.setAssetKey(AssetTestUtil.FIELD1_ASSET_KEY);

    when(assetService.getAsset(AssetTestUtil.FIELD1_ASSET_KEY))
        .thenReturn(AssetTestUtil.field1AssetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field1AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithNullOperatorAndLicences);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_LICENCES
                .formatted(customerConfigurationProperties.mnemonic())))
    );
  }

  @Test
  void validate_fieldAssetWithLicences() {
    form.setAssetKey(AssetTestUtil.FIELD1_ASSET_KEY);

    when(assetService.getAsset(AssetTestUtil.FIELD1_ASSET_KEY))
        .thenReturn(AssetTestUtil.field1AssetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field1AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithOperatorAndLicences);

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }
}
