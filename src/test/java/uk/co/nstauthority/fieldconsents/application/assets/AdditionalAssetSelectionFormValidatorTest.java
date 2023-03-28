package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.AdditionalAssetsControllerTest.ASSET_KEY;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset2;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
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

  @Mock
  private ApplicationAssetService applicationAssetService;

  private final CustomerConfigurationProperties customerConfigurationProperties
      = ValidatorTestingUtil.getCustomerConfigurationProperties();

  private AdditionalAssetSelectionFormValidator validator;

  private Errors errors;

  private Map<String, List<String>> errorMap;

  private AssetSelectionForm form;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    validator = new AdditionalAssetSelectionFormValidator(
        assetService,
        fieldService,
        customerConfigurationProperties,
        applicationAssetService);
    form = new AssetSelectionForm(ASSET_KEY, applicationVersion);
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void validate_emptyForm() {
    form.setAssetKey(null);
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
  void validate_fieldAssetNoOperatorButLicencesExist() {
    form.setAssetKey(AssetTestUtil.FIELD1_ASSET_KEY);

    when(assetService.getAsset(AssetTestUtil.FIELD1_ASSET_KEY))
        .thenReturn(AssetTestUtil.field1AssetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field1AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithNoOperatorButLicences);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR +
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL
                    .formatted(customerConfigurationProperties.email())))
    );
  }

  @Test
  void validate_fieldAssetOperatorButNoLicences() {
    form.setAssetKey(AssetTestUtil.FIELD1_ASSET_KEY);

    when(assetService.getAsset(AssetTestUtil.FIELD1_ASSET_KEY))
        .thenReturn(AssetTestUtil.field1AssetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field1AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithOperatorButEmptyLicences);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_LICENCES +
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL
                    .formatted(customerConfigurationProperties.email())))
    );
  }

  @Test
  void validate_fieldAssetNoOperatorOrLicences() {
    form.setAssetKey(AssetTestUtil.FIELD1_ASSET_KEY);

    when(assetService.getAsset(AssetTestUtil.FIELD1_ASSET_KEY))
        .thenReturn(AssetTestUtil.field1AssetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field1AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithNullOperatorAndLicences);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR_LICENCES +
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL
                    .formatted(customerConfigurationProperties.email())))
    );
  }

  @Test
  void validate_fieldAssetWithOperatorAndLicences() {
    form.setAssetKey(AssetTestUtil.FIELD1_ASSET_KEY);

    when(assetService.getAsset(AssetTestUtil.FIELD1_ASSET_KEY))
        .thenReturn(AssetTestUtil.field1AssetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field1AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithOperatorAndLicences);

    ValidationUtils.invokeValidator(validator, form, errors);

    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void validate_fieldAssetWithDuplicatedPrimaryAsset() {
    form.setAssetKey(AssetTestUtil.FIELD1_ASSET_KEY);

    when(applicationAssetService.findByApplicationVersionAndFieldId(applicationVersion, fieldAsset1.getFieldId())).thenReturn(
        Optional.of(fieldAsset1));
    when(assetService.getAsset(AssetTestUtil.FIELD1_ASSET_KEY))
        .thenReturn(AssetTestUtil.field1AssetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field1AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithOperatorAndLicences);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.DUPLICATED_PRIMARY_FIELD.formatted(AssetTestUtil.field1AssetJson.getName()))));
  }

  @Test
  void validate_fieldAssetWithDuplicatedSecondaryAsset() {
    form.setAssetKey(AssetTestUtil.FIELD2_ASSET_KEY);

    when(applicationAssetService.findByApplicationVersionAndFieldId(applicationVersion, fieldAsset2.getFieldId())).thenReturn(
        Optional.of(fieldAsset2));
    when(assetService.getAsset(AssetTestUtil.FIELD2_ASSET_KEY))
        .thenReturn(AssetTestUtil.field2AssetJson);
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field2AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field2JsonWithOperatorAndLicences);

    ValidationUtils.invokeValidator(validator, form, errors);

    errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(errors);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.DUPLICATED_SECONDARY_FIELD.formatted(AssetTestUtil.field2AssetJson.getName()))));
  }
}
