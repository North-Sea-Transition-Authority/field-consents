package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset2;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset3;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.secondaryAssets;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.terminalAsset1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNullOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@ExtendWith(MockitoExtension.class)
class ApplicationAssetServiceTest {

  @Mock
  private ApplicationAssetRepository applicationAssetRepository;

  @Mock
  private FieldService fieldService;

  @Mock
  private TerminalService terminalService;

  @Mock
  private ApplicationFlagService applicationFlagService;

  @Mock
  private AssetService assetService;

  @Captor
  private ArgumentCaptor<ApplicationAsset> assetArgumentCaptor;

  private ApplicationAssetService applicationAssetService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationAssetService = new ApplicationAssetService(
        fieldService,
        terminalService,
        applicationAssetRepository,
        applicationFlagService,
        assetService
    );
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
  }

  @Test
  void createPrimaryAsset_field() {
    applicationAssetService.createPrimaryAsset(applicationVersion, field1JsonWithOperator);

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset)
        .extracting(
            ApplicationAsset::getApplicationVersion,
            ApplicationAsset::getAssetRole,
            ApplicationAsset::getFieldId,
            ApplicationAsset::getCachedFieldName,
            ApplicationAsset::getAssetOperatorOuId,
            ApplicationAsset::getCachedAssetOperatorName,
            ApplicationAsset::getTerminalId,
            ApplicationAsset::getCachedTerminalName
        )
        .containsExactly(
            applicationVersion,
            AssetRole.PRIMARY,
            field1JsonWithOperator.getId(),
            field1JsonWithOperator.getName(),
            field1JsonWithOperator.getOperatorJson().organisationUnitId(),
            field1JsonWithOperator.getOperatorJson().name(),
            null,
            null
        );
  }

  @Test
  void createPrimaryAsset_fieldWillNoOperator() {
    assertThatThrownBy(() -> applicationAssetService.createPrimaryAsset(applicationVersion, field1JsonWithNullOperatorAndLicences))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("No operator was found for asset %s with id %s."
            .formatted(field1JsonWithNullOperatorAndLicences.getName(),
                field1JsonWithNullOperatorAndLicences.getId()));
  }

  @Test
  void createPrimaryAsset_terminal() {
    applicationAssetService.createPrimaryAsset(applicationVersion, terminal1JsonWithOperator);

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset)
        .extracting(
            ApplicationAsset::getApplicationVersion,
            ApplicationAsset::getAssetRole,
            ApplicationAsset::getFieldId,
            ApplicationAsset::getCachedFieldName,
            ApplicationAsset::getAssetOperatorOuId,
            ApplicationAsset::getCachedAssetOperatorName,
            ApplicationAsset::getTerminalId,
            ApplicationAsset::getCachedTerminalName
        )
        .containsExactly(
            applicationVersion,
            AssetRole.PRIMARY,
            null,
            null,
            terminal1JsonWithOperator.getOperatorJson().organisationUnitId(),
            terminal1JsonWithOperator.getOperatorJson().name(),
            terminal1JsonWithOperator.getId(),
            terminal1JsonWithOperator.getName()
        );
  }

  @Test
  void createSecondaryAsset_terminal() {
    assertThatThrownBy(() -> applicationAssetService.createSecondaryAsset(applicationVersion, terminal1JsonWithOperator))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Secondary asset of type terminal not allowed for application version id %s asset id %s"
            .formatted(applicationVersion.getId(), terminal1JsonWithOperator.getId()));
  }

  @Test
  void createSecondaryAsset_firstOne() {
    when(applicationAssetRepository
        .findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(Collections.emptyList());

    applicationAssetService.createSecondaryAsset(applicationVersion, field1JsonWithOperator);

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset)
        .extracting(
            ApplicationAsset::getApplicationVersion,
            ApplicationAsset::getAssetRole,
            ApplicationAsset::getFieldId,
            ApplicationAsset::getCachedFieldName,
            ApplicationAsset::getAssetOperatorOuId,
            ApplicationAsset::getCachedAssetOperatorName,
            ApplicationAsset::getTerminalId,
            ApplicationAsset::getCachedTerminalName,
            ApplicationAsset::getAssetNo
        )
        .containsExactly(
            applicationVersion,
            AssetRole.SECONDARY,
            field1JsonWithOperator.getId(),
            field1JsonWithOperator.getName(),
            field1JsonWithOperator.getOperatorJson().organisationUnitId(),
            field1JsonWithOperator.getOperatorJson().name(),
            null,
            null,
            1
        );
  }

  @Test
  void createSecondaryAsset_fourthOne() {
    when(applicationAssetRepository
        .findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(secondaryAssets);

    applicationAssetService.createSecondaryAsset(applicationVersion, field1JsonWithOperator);

    verify(applicationAssetRepository, times(1)).save(assetArgumentCaptor.capture());

    ApplicationAsset capturedAsset = assetArgumentCaptor.getValue();
    assertThat(capturedAsset)
        .extracting(
            ApplicationAsset::getApplicationVersion,
            ApplicationAsset::getAssetRole,
            ApplicationAsset::getFieldId,
            ApplicationAsset::getCachedFieldName,
            ApplicationAsset::getAssetOperatorOuId,
            ApplicationAsset::getCachedAssetOperatorName,
            ApplicationAsset::getTerminalId,
            ApplicationAsset::getCachedTerminalName,
            ApplicationAsset::getAssetNo
        )
        .containsExactly(
            applicationVersion,
            AssetRole.SECONDARY,
            field1JsonWithOperator.getId(),
            field1JsonWithOperator.getName(),
            field1JsonWithOperator.getOperatorJson().organisationUnitId(),
            field1JsonWithOperator.getOperatorJson().name(),
            null,
            null,
            4
        );
  }

  private ApplicationAsset getStubPrimaryApplicationAsset(ApplicationVersion applicationVersion) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setAssetRole(AssetRole.PRIMARY);
    return applicationAsset;
  }

  @Test
  void getPrimaryAsset_exists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    when(applicationAssetRepository.findByApplicationVersionAndAssetRole(applicationVersion, AssetRole.PRIMARY))
        .thenReturn(Optional.of(applicationAsset));
    assertThat(applicationAssetService.getPrimaryAsset(applicationVersion))
        .isEqualTo(applicationAsset);
  }

  @Test
  void getPrimaryAsset_noneExists() {
    when(applicationAssetRepository.findByApplicationVersionAndAssetRole(applicationVersion, AssetRole.PRIMARY))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationAssetService.getPrimaryAsset(applicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Primary application asset not found for application version id %s"
            .formatted(applicationVersion.getId()));
  }

  @Test
  void getSecondaryAsset_noAssetFound() {
    when(applicationAssetRepository.findByApplicationVersionAndAssetNo(applicationVersion, 1)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> applicationAssetService.getSecondaryAsset(applicationVersion, 1))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining(String.format("Asset with application version id %s and asset no %s not found", applicationVersion.getId(), 1));
  }

  @Test
  void getSecondaryAsset_assetExists() {
    when(applicationAssetRepository.findByApplicationVersionAndAssetNo(applicationVersion, 1))
        .thenReturn(Optional.of(fieldAsset1));

    assertThat(applicationAssetService.getSecondaryAsset(applicationVersion, 1)).isEqualTo(fieldAsset1);
  }

  @Test
  void getSecondaryAssets_noAssets() {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(Collections.emptyList());

    assertThat(applicationAssetService.getSecondaryAssets(applicationVersion)).isEmpty();
  }

  @Test
  void getSecondaryAssets_withAssets() {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(secondaryAssets);

    assertThat(applicationAssetService.getSecondaryAssets(applicationVersion))
        .containsExactly(
            fieldAsset2,
            fieldAsset3);
  }

  @Test
  void secondaryAssetsExist_doNotExist() {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(Collections.emptyList());

    assertThat(applicationAssetService.secondaryAssetsExist(applicationVersion)).isFalse();
  }

  @Test
  void secondaryAssetsExist_exist() {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY))
        .thenReturn(secondaryAssets);

    assertThat(applicationAssetService.secondaryAssetsExist(applicationVersion)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(AssetRole.class)
  void assetExistsForApplicationVersionAndAssetRole_exists(AssetRole assetRole) {
    when(applicationAssetRepository.existsByApplicationVersionAndAssetRole(applicationVersion, assetRole)).thenReturn(true);
    assertThat(applicationAssetService.assetExistsForApplicationVersionAndAssetRole(applicationVersion, assetRole)).isTrue();
  }

  @ParameterizedTest
  @EnumSource(AssetRole.class)
  void assetExistsForApplicationVersionAndAssetRole_doesNotExist(AssetRole assetRole) {
    when(applicationAssetRepository.existsByApplicationVersionAndAssetRole(applicationVersion, assetRole)).thenReturn(false);
    assertThat(applicationAssetService.assetExistsForApplicationVersionAndAssetRole(applicationVersion, assetRole)).isFalse();
  }

  @Test
  void deleteSecondaryAsset_primaryFails() {
    assertThatThrownBy(() -> applicationAssetService.deleteSecondaryAsset(fieldAsset1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot delete asset with id:%s and role:%s"
            .formatted(fieldAsset1.getId(), fieldAsset1.getAssetRole()));
  }

  @Test
  void deleteSecondaryAsset_secondarySuccess() {
    applicationAssetService.deleteSecondaryAsset(fieldAsset2);

    verify(applicationAssetRepository, times(1)).delete(fieldAsset2);
  }

  @Test
  void getAssetJsonForApplicationAsset_noAssetIdsExists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    assertThatThrownBy(() -> applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Field and terminal ids not found for application asset id %s"
            .formatted(applicationAsset.getId()));
  }

  @Test
  void getAssetJsonForApplicationAsset_fieldExists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setFieldId(field1.getFieldId());
    when(fieldService.findField(eq(field1.getFieldId()), any())).thenReturn(Optional.of(field1Json));

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(field1Json);
  }

  @Test
  void getAssetJsonForApplicationAsset_fieldIdExistsButNameLookupFails() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setFieldId(field1.getFieldId());
    applicationAsset.setCachedFieldName(field1.getFieldName());

    when(fieldService.findField(eq(field1.getFieldId()), any())).thenReturn(Optional.empty());

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .extracting(
            AssetJson::getId,
            AssetJson::getName,
            AssetJson::getAssetType
        )
        .containsExactly(
            field1.getFieldId(),
            field1.getFieldName(),
            AssetType.FIELD
        );
  }

  @Test
  void getAssetJsonForApplicationAsset_terminalExists() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setTerminalId(terminal1.getTerminalId());
    when(terminalService.findTerminal(eq(terminal1.getTerminalId()), any()))
        .thenReturn(Optional.of(terminal1Json));

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .isEqualTo(terminal1Json);
  }

  @Test
  void getAssetJsonForApplicationAsset_terminalIdExistsButNameLookupFails() {
    ApplicationAsset applicationAsset = getStubPrimaryApplicationAsset(applicationVersion);
    applicationAsset.setTerminalId(terminal1.getTerminalId());
    applicationAsset.setCachedTerminalName(terminal1.getTerminalName());
    when(terminalService.findTerminal(eq(terminal1.getTerminalId()), any()))
        .thenReturn(Optional.empty());

    assertThat(applicationAssetService.getAssetJsonForApplicationAsset(applicationAsset))
        .extracting(
            AssetJson::getId,
            AssetJson::getName,
            AssetJson::getAssetType
        )
        .containsExactly(
            terminal1.getTerminalId(),
            terminal1.getTerminalName(),
            AssetType.TERMINAL
        );
  }


  @Test
  void getAdditionalAssetsSetupForm_whenTrue() {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.of(true));

    AdditionalAssetsSetupForm form = applicationAssetService.getAdditionalAssetsSetupForm(applicationVersion);

    assertThat(form.getOtherAssetsRequired()).isTrue();
  }

  @Test
  void getAdditionalAssetsSetupForm_whenFalse() {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.of(false));

    AdditionalAssetsSetupForm form = applicationAssetService.getAdditionalAssetsSetupForm(applicationVersion);

    assertThat(form.getOtherAssetsRequired()).isFalse();
  }

  @Test
  void getAdditionalAssetsSetupForm_whenNotExist() {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS))
        .thenReturn(Optional.empty());

    AdditionalAssetsSetupForm form = applicationAssetService.getAdditionalAssetsSetupForm(applicationVersion);

    assertThat(form.getOtherAssetsRequired()).isNull();
  }

  @Test
  void findByApplicationVersionAndFieldId_whenExists() {
    when(applicationAssetRepository.findByApplicationVersionAndFieldId(applicationVersion, FIELD_ID_1))
        .thenReturn(Optional.of(fieldAsset1));

    assertThat(applicationAssetService.findByApplicationVersionAndFieldId(applicationVersion, FIELD_ID_1)).get()
        .usingRecursiveComparison()
        .isEqualTo(fieldAsset1);
  }

  @Test
  void findByApplicationVersionAndFieldId_whenNotExists() {
    when(applicationAssetRepository.findByApplicationVersionAndFieldId(applicationVersion, FIELD_ID_1))
        .thenReturn(Optional.empty());

    assertThat(applicationAssetService.findByApplicationVersionAndFieldId(applicationVersion, FIELD_ID_1)).isNotPresent();
  }

  @Test
  void findAllPrimaryFieldAssets_emptyList() {
    when(applicationAssetRepository.findAllByAssetRoleAndFieldIdIsNotNull(AssetRole.PRIMARY)).thenReturn(Collections.emptyList());
    assertThat(applicationAssetService.findAllPrimaryFieldAssets()).isEmpty();
  }

  @Test
  void findAllPrimaryFieldAssets_nonEmptyList() {
    when(applicationAssetRepository.findAllByAssetRoleAndFieldIdIsNotNull(AssetRole.PRIMARY)).thenReturn(List.of(fieldAsset1));
    var primaryFieldAssets = applicationAssetService.findAllPrimaryFieldAssets();

    assertThat(primaryFieldAssets).hasSize(1);
    assertThat(primaryFieldAssets.get(0)).usingRecursiveComparison().isEqualTo(fieldAsset1);
  }

  @Test
  void findAllPrimaryTerminalAssets_emptyList() {
    when(applicationAssetRepository.findAllByAssetRoleAndTerminalIdIsNotNull(AssetRole.PRIMARY)).thenReturn(Collections.emptyList());
    assertThat(applicationAssetService.findAllPrimaryTerminalAssets()).isEmpty();
  }

  @Test
  void findAllPrimaryTerminalAssets_nonEmptyList() {
    when(applicationAssetRepository.findAllByAssetRoleAndTerminalIdIsNotNull(AssetRole.PRIMARY)).thenReturn(List.of(terminalAsset1));
    var primaryFieldAssets = applicationAssetService.findAllPrimaryTerminalAssets();

    assertThat(primaryFieldAssets).hasSize(1);
    assertThat(primaryFieldAssets.get(0)).usingRecursiveComparison().isEqualTo(terminalAsset1);
  }

  @ParameterizedTest
  @EnumSource(AssetRole.class)
  void getAssetJsonListFor_field(AssetRole assetRole) {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, assetRole))
        .thenReturn(Collections.singletonList(fieldAsset1));
    when(fieldService.getField(eq(fieldAsset1.getId()), anyString()))
        .thenReturn(field1Json);

    assertThat(applicationAssetService.getAssetJsonListFor(applicationVersion, assetRole))
        .isEqualTo(Collections.singletonList(field1Json));
  }

  @ParameterizedTest
  @EnumSource(AssetRole.class)
  void getAssetJsonListFor_terminal(AssetRole assetRole) {
    when(applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, assetRole))
        .thenReturn(Collections.singletonList(terminalAsset1));
    when(terminalService.getTerminal(eq(terminalAsset1.getId()), anyString()))
        .thenReturn(terminal1Json);

    assertThat(applicationAssetService.getAssetJsonListFor(applicationVersion, assetRole))
        .isEqualTo(Collections.singletonList(terminal1Json));
  }

  @ParameterizedTest
  @EnumSource(AssetRole.class)
  void createAssetForApplicationVersion_field(AssetRole assetRole) {
    var assetKey = "assetKey";

    when(assetService.getAsset(assetKey)).thenReturn(field1Json);
    when(fieldService.getFieldWithOperator(eq(field1.getFieldId()), anyString())).thenReturn(field1JsonWithOperator);

    applicationAssetService.createAssetForApplicationVersion(applicationVersion, assetKey, assetRole);

    var applicationAssetCaptor = ArgumentCaptor.forClass(ApplicationAsset.class);
    verify(applicationAssetRepository).save(applicationAssetCaptor.capture());

    assertThat(applicationAssetCaptor.getValue())
        .extracting(
            ApplicationAsset::getApplicationVersion,
            ApplicationAsset::getFieldId,
            ApplicationAsset::getAssetRole
        ).containsExactly(
            applicationVersion,
            field1.getFieldId(),
            assetRole
        );
  }

  @ParameterizedTest
  @EnumSource(AssetRole.class)
  void createAssetForApplicationVersion_terminal(AssetRole assetRole) {
    var assetKey = "assetKey";

    when(assetService.getAsset(assetKey)).thenReturn(terminal1Json);
    when(terminalService.getTerminalWithOperator(eq(terminal1.getTerminalId()), anyString())).thenReturn(terminal1JsonWithOperator);

    applicationAssetService.createAssetForApplicationVersion(applicationVersion, assetKey, assetRole);

    var applicationAssetCaptor = ArgumentCaptor.forClass(ApplicationAsset.class);
    verify(applicationAssetRepository).save(applicationAssetCaptor.capture());

    assertThat(applicationAssetCaptor.getValue())
        .extracting(
            ApplicationAsset::getApplicationVersion,
            ApplicationAsset::getTerminalId,
            ApplicationAsset::getAssetRole
        ).containsExactly(
            applicationVersion,
            terminal1.getTerminalId(),
            assetRole
        );
  }

  @Test
  void deleteAssetsByApplicationVersionAndAssetRoles() {
    var assetRoles = Set.of(AssetRole.HOST, AssetRole.LOCATION);
    applicationAssetService.deleteAssetsByApplicationVersionAndAssetRoles(applicationVersion, assetRoles);

    verify(applicationAssetRepository).deleteAllByApplicationVersionAndAssetRoleIn(applicationVersion, assetRoles);
  }

  @Test
  void getPrimaryAndSecondaryFieldJsonsOfShoreType_withNoPrimaryOrSecondaryFields() {
    var requestPurpose = "lookup fields";
    when(applicationAssetRepository.findAllByFieldIdIsNotNullAndAssetRoleIn(EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(Collections.emptyList());
    var assetTypesWithShore = List.of(AssetTypeWithShore.FIELD_OFFSHORE, AssetTypeWithShore.TERMINAL);

    assertThat(applicationAssetService.getPrimaryAndSecondaryFieldJsonsOfShoreType(assetTypesWithShore, requestPurpose))
        .isEmpty();
  }

  @Test
  void getPrimaryAndSecondaryFieldJsonsOfShoreType_withOnlyOneFieldMatchingAssetTypeWithShore() {
    var requestPurpose = "lookup fields";
    var fieldAssets = List.of(fieldAsset1, fieldAsset2);
    when(applicationAssetRepository.findAllByFieldIdIsNotNullAndAssetRoleIn(EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(fieldAssets);

    var fieldIds = fieldAssets.stream().map(ApplicationAsset::getFieldId)
        .distinct()
        .toList();
    var assetTypesWithShore = List.of(AssetTypeWithShore.FIELD_OFFSHORE, AssetTypeWithShore.TERMINAL);
    when(fieldService.findFieldsByIds(fieldIds, requestPurpose))
        .thenReturn(List.of(field1Json, field2Json));

    assertThat(applicationAssetService.getPrimaryAndSecondaryFieldJsonsOfShoreType(assetTypesWithShore, requestPurpose))
        .containsExactly(field1Json);
  }

  @Test
  void getPrimaryAndSecondaryFieldJsonsOfShoreType_withAllFieldsMatchingAssetTypeWithShore() {
    var requestPurpose = "lookup fields";
    var fieldAssets = List.of(fieldAsset1, fieldAsset2);
    when(applicationAssetRepository.findAllByFieldIdIsNotNullAndAssetRoleIn(EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(fieldAssets);

    var fieldIds = fieldAssets.stream().map(ApplicationAsset::getFieldId)
        .distinct()
        .toList();
    var assetTypesWithShore = List.of(AssetTypeWithShore.FIELD_OFFSHORE, AssetTypeWithShore.FIELD_ONSHORE);
    when(fieldService.findFieldsByIds(fieldIds, requestPurpose))
        .thenReturn(List.of(field1Json, field2Json));

    assertThat(applicationAssetService.getPrimaryAndSecondaryFieldJsonsOfShoreType(assetTypesWithShore, requestPurpose))
        .containsExactly(field1Json, field2Json);
  }
}
