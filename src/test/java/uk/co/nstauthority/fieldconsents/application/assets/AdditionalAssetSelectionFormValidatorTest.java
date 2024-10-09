package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset2;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field2AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field4AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field4JsonWithOperatorAndLicences;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.AssetSelectionForm;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetTestUtil;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.branding.BrandingTestUtil;
import uk.co.nstauthority.fieldconsents.validation.ValidatorTestingUtil;

@ExtendWith(MockitoExtension.class)
class AdditionalAssetSelectionFormValidatorTest {

  @Mock
  private AssetService assetService;

  @Mock
  private FieldService fieldService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  private AdditionalAssetSelectionFormValidator validator;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    validator = new AdditionalAssetSelectionFormValidator(
        assetService,
        fieldService,
        BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES,
        applicationAssetService
    );
  }

  @Test
  void validate_emptyForm() {
    var form = AssetSelectionForm.empty();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    validator.validate(form, bindingResult, applicationVersion);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);

    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(AdditionalAssetSelectionFormValidator.ASSET_EMPTY))
    );
  }

  @Test
  void validate_terminalAsset() {
    var assetJson = terminal1AssetJson;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(assetService.findAsset(assetJson.getAssetKey())).thenReturn(Optional.of(assetJson));

    validator.validate(form, bindingResult, applicationVersion);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(AdditionalAssetSelectionFormValidator.ASSET_MUST_BE_FIELD))
    );
  }

  @Test
  void validate_fieldAssetWithInvalidStatus() {
    var assetJson = field4AssetJson;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(assetService.findAsset(assetJson.getAssetKey())).thenReturn(Optional.of(assetJson));
    when(fieldService.getFieldWithOperatorAndLicences(eq(assetJson.getId()), any()))
        .thenReturn(field4JsonWithOperatorAndLicences);

    validator.validate(form, bindingResult, applicationVersion);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_ALLOWED_STATUS
                    .formatted(field4AssetJson.getName())))
    );
  }

  @Test
  void validate_fieldAssetNoOperatorButLicencesExist() {
    var assetJson = field1AssetJson;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(assetService.findAsset(assetJson.getAssetKey())).thenReturn(Optional.of(assetJson));
    when(fieldService.getFieldWithOperatorAndLicences(eq(assetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithNoOperatorButLicences);

    validator.validate(form, bindingResult, applicationVersion);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR
                    .formatted(AssetTestUtil.field1AssetJson.getName()) +
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL
                    .formatted(BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.email())))
    );
  }

  @Test
  void validate_fieldAssetOperatorButNoLicences() {
    var assetJson = field1AssetJson;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(assetService.findAsset(assetJson.getAssetKey())).thenReturn(Optional.of(assetJson));
    when(fieldService.getFieldWithOperatorAndLicences(eq(assetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithOperatorButEmptyLicences);

    validator.validate(form, bindingResult, applicationVersion);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_LICENCES
                    .formatted(AssetTestUtil.field1AssetJson.getName())+
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL
                    .formatted(BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.email())))
    );
  }

  @Test
  void validate_fieldAssetNoOperatorOrLicences() {
    var assetJson = field1AssetJson;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(assetService.findAsset(assetJson.getAssetKey())).thenReturn(Optional.of(assetJson));
    when(fieldService.getFieldWithOperatorAndLicences(eq(assetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithNullOperatorAndLicences);

    validator.validate(form, bindingResult, applicationVersion);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR_LICENCES
                    .formatted(AssetTestUtil.field1AssetJson.getName())+
                AdditionalAssetSelectionFormValidator.ASSET_MUST_HAVE_OPERATOR_LICENCES_TAIL
                    .formatted(BrandingTestUtil.CUSTOMER_BRANDING_CONFIGURATION_PROPERTIES.email())))
    );
  }

  @Test
  void validate_fieldAssetWithOperatorAndLicences() {
    var assetJson = field1AssetJson;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(assetService.findAsset(assetJson.getAssetKey())).thenReturn(Optional.of(assetJson));
    when(fieldService.getFieldWithOperatorAndLicences(eq(assetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithOperatorAndLicences);

    validator.validate(form, bindingResult, applicationVersion);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  void validate_fieldAssetWithDuplicatedPrimaryAsset() {
    var assetJson = field1AssetJson;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(List.of(fieldAsset1));
    when(assetService.findAsset(assetJson.getAssetKey())).thenReturn(Optional.of(assetJson));
    when(fieldService.getFieldWithOperatorAndLicences(eq(assetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field1JsonWithOperatorAndLicences);

    validator.validate(form, bindingResult, applicationVersion);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.DUPLICATED_PRIMARY_FIELD.formatted(AssetTestUtil.field1AssetJson.getName()))));
  }

  @Test
  void validate_fieldAssetWithDuplicatedSecondaryAsset() {
    var assetJson = field2AssetJson;
    var form = AssetSelectionForm.from(assetJson.getAssetKey());
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(List.of(fieldAsset2));
    when(assetService.findAsset(assetJson.getAssetKey())).thenReturn(Optional.of(assetJson));
    when(fieldService.getFieldWithOperatorAndLicences(eq(AssetTestUtil.field2AssetJson.getId()), any()))
        .thenReturn(FieldTestUtil.field2JsonWithOperatorAndLicences);

    validator.validate(form, bindingResult, applicationVersion);

    var errorMap = ValidatorTestingUtil.getErrorsFieldsAndMessages(bindingResult);
    assertThat(errorMap).containsOnly(
        entry(AdditionalAssetSelectionFormValidator.ASSET_KEY_FIELD_NAME,
            Collections.singletonList(
                AdditionalAssetSelectionFormValidator.DUPLICATED_SECONDARY_FIELD.formatted(AssetTestUtil.field2AssetJson.getName()))));
  }
}
