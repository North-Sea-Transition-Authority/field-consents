package uk.co.nstauthority.fieldconsents.application.assets;

import static uk.co.nstauthority.fieldconsents.assets.AssetType.FIELD;
import static uk.co.nstauthority.fieldconsents.assets.AssetType.TERMINAL;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetKey;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.facility.FacilityService;
import uk.co.nstauthority.fieldconsents.assets.facility.FacilityWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.Shore;
import uk.co.nstauthority.fieldconsents.assets.hubs.HubService;
import uk.co.nstauthority.fieldconsents.assets.hubs.HubWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Service
public class ApplicationAssetService {

  private final FieldApi fieldApi;
  private final FieldService fieldService;
  private final TerminalService terminalService;
  private final FacilityService facilityService;
  private final ApplicationAssetRepository applicationAssetRepository;
  private final ApplicationFlagService applicationFlagService;
  private final HubService hubService;

  ApplicationAssetService(
      FieldApi fieldApi,
      FieldService fieldService,
      TerminalService terminalService,
      FacilityService facilityService,
      ApplicationAssetRepository applicationAssetRepository,
      ApplicationFlagService applicationFlagService,
      HubService hubService
  ) {
    this.fieldApi = fieldApi;
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.facilityService = facilityService;
    this.applicationAssetRepository = applicationAssetRepository;
    this.applicationFlagService = applicationFlagService;
    this.hubService = hubService;
  }

  private ApplicationAsset createAsset(
      ApplicationVersion applicationVersion,
      AssetWithOperatorJson asset,
      AssetRole assetRole
  ) {
    var applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setAssetId(asset.getId());
    applicationAsset.setCachedAssetName(asset.getName());
    applicationAsset.setAssetType(asset.getAssetType());
    applicationAsset.setAssetRole(assetRole);

    var operatorJson = asset.getOperatorJson();
    applicationAsset.setAssetOperatorOuId(operatorJson.organisationUnitId());
    applicationAsset.setCachedAssetOperatorName(operatorJson.name());

    return applicationAsset;
  }

  public ApplicationAsset createPrimaryAsset(ApplicationVersion applicationVersion, AssetWithOperatorJson asset) {
    var applicationAsset = createAsset(applicationVersion, asset, AssetRole.PRIMARY);
    return applicationAssetRepository.save(applicationAsset);
  }

  public ApplicationAsset createSecondaryAsset(ApplicationVersion applicationVersion,
                                               AssetWithOperatorJson asset) {
    if (asset.getAssetType().equals(TERMINAL)) {
      throw new IllegalArgumentException(
          "Secondary asset of type terminal not allowed for application version id %s asset id %s"
              .formatted(applicationVersion.getId(), asset.getId()));
    }

    var applicationAsset = createAsset(applicationVersion, asset, AssetRole.SECONDARY);

    // find the next Asset number to use
    Integer nextAssetNo = getSecondaryAssets(applicationVersion).stream()
        .max(Comparator.comparing(ApplicationAsset::getAssetNo))
        .map(lastAsset -> lastAsset.getAssetNo() + 1)
        .orElse(1);

    applicationAsset.setAssetNo(nextAssetNo);

    return applicationAssetRepository.save(applicationAsset);
  }

  public ApplicationAsset getPrimaryAsset(ApplicationVersion applicationVersion) {
    return applicationAssetRepository.findByApplicationVersionAndAssetRole(applicationVersion, AssetRole.PRIMARY)
        .orElseThrow(() -> new EntityNotFoundException("Primary application asset not found for application version id %s"
            .formatted(applicationVersion.getId())));
  }

  public ApplicationAsset getSecondaryAsset(ApplicationVersion applicationVersion, Integer assetNo) {
    return applicationAssetRepository.findByApplicationVersionAndAssetNo(applicationVersion, assetNo)
        .orElseThrow(() ->
            new EntityNotFoundException("Asset with application version id %s and asset no %s not found"
                .formatted(applicationVersion.getId(), assetNo))
        );
  }

  public List<ApplicationAsset> findAssetsByApplicationVersionAndAssetRoles(
      ApplicationVersion applicationVersion,
      Collection<AssetRole> assetRoles
  ) {
    return applicationAssetRepository.findAllByApplicationVersionAndAssetRoleInOrderByIdAsc(
        applicationVersion,
        assetRoles
    );
  }

  public List<ApplicationAsset> findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
      ApplicationVersion applicationVersion,
      AssetType assetType,
      Collection<AssetRole> assetRoles
  ) {
    return applicationAssetRepository.findAllByApplicationVersionAndAssetTypeAndAssetRoleInOrderByIdAsc(
        applicationVersion,
        assetType,
        assetRoles
    );
  }

  public List<ApplicationAsset> findAssetsByApplicationVersion(ApplicationVersion applicationVersion) {
    return applicationAssetRepository.findAllByApplicationVersion(applicationVersion);
  }

  public List<ApplicationAsset> getSecondaryAssets(ApplicationVersion applicationVersion) {
    return findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Collections.singleton(AssetRole.SECONDARY));
  }

  public boolean secondaryAssetsExist(ApplicationVersion applicationVersion) {
    return !getSecondaryAssets(applicationVersion).isEmpty();
  }

  public boolean assetExistsForApplicationVersionAndAssetRole(ApplicationVersion applicationVersion, AssetRole assetRole) {
    return applicationAssetRepository.existsByApplicationVersionAndAssetRole(applicationVersion, assetRole);
  }

  @Transactional
  public void deleteSecondaryAsset(ApplicationAsset asset) {
    if (asset.getAssetRole() != AssetRole.SECONDARY) {
      throw new IllegalArgumentException("Cannot delete asset with id:%s and role:%s"
          .formatted(asset.getId(), asset.getAssetRole()));
    }
    applicationAssetRepository.delete(asset);
  }

  public AssetJson getAssetJsonForApplicationAsset(ApplicationAsset applicationAsset) {
    var assetId = applicationAsset.getAssetId();
    return switch (applicationAsset.getAssetType()) {
      case FIELD -> fieldService
          .findField(assetId, "Field lookup for application asset")
          .orElseGet(() -> FieldJson.fromCachedInformation(assetId, applicationAsset.getCachedAssetName()));
      case TERMINAL -> terminalService
          .findTerminal(assetId, "Terminal lookup for application asset")
          .orElseGet(() -> TerminalJson.fromCachedInformation(assetId, applicationAsset.getCachedAssetName()));
      case FACILITY -> facilityService
          .findFacilityWithOperator(assetId, "Facility lookup for application asset")
          .orElseGet(() -> FacilityWithOperatorJson.fromCachedInformation(assetId, applicationAsset.getCachedAssetName()));
      case HUB -> hubService
          .findHubWithOperator(assetId, "Hub lookup for application asset")
          .orElseGet(() -> HubWithOperatorJson.fromCachedInformation(assetId, applicationAsset.getCachedAssetName()));
    };
  }

  public AdditionalAssetsSetupForm getAdditionalAssetsSetupForm(ApplicationVersion applicationVersion) {
    AdditionalAssetsSetupForm form = new AdditionalAssetsSetupForm();
    Optional<Boolean> optionalFlag = applicationFlagService
        .findFlagValue(applicationVersion, ApplicationFlagType.HAS_SECONDARY_ASSETS);

    optionalFlag.ifPresent(
        form::setOtherAssetsRequired
    );
    return form;
  }

  public List<ApplicationAsset> findAllPrimaryFieldAssets() {
    return applicationAssetRepository.findAllByAssetRoleAndAssetTypeAndAssetIdIsNotNull(AssetRole.PRIMARY, FIELD);
  }

  public List<ApplicationAsset> findAllPrimaryTerminalAssets() {
    return applicationAssetRepository.findAllByAssetRoleAndAssetTypeAndAssetIdIsNotNull(AssetRole.PRIMARY, TERMINAL);
  }

  public List<FieldJson> getPrimaryAndSecondaryFieldJsonsOfShoreType(Collection<AssetTypeWithShore> assetTypeWithShores,
                                                                     String requestPurpose) {
    var shores = assetTypeWithShores.stream()
        .map(AssetTypeWithShore::getShore)
        .toList();

    var primaryAndSecondaryFieldIds = getAllPrimaryAndSecondaryFieldAssets()
        .stream()
        .map(ApplicationAsset::getAssetId)
        .distinct()
        .toList();

    return fieldService.findFieldsByIds(primaryAndSecondaryFieldIds, requestPurpose)
        .stream()
        .filter(fieldJson -> shores.contains(fieldJson.getShore()))
        .toList();
  }

  public List<ApplicationAsset> getAllPrimaryAndSecondaryFieldAssets() {
    return applicationAssetRepository
        .findAllByAssetIdIsNotNullAndAssetRoleInAndAssetTypeIn(
            EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY),
            Collections.singleton(FIELD)
        )
        .stream()
        .toList();
  }

  public List<AssetJson> getAssetJsonListFor(ApplicationVersion applicationVersion, AssetRole assetRole) {
    return findAssetsByApplicationVersionAndAssetRoles(applicationVersion, Collections.singleton(assetRole))
        .stream()
        .map(this::getAssetJsonForApplicationAsset)
        .toList();
  }

  public boolean consentedProductionApplicationExistsWithPrimaryField(Integer fieldId) {
    return applicationAssetRepository
        .existsByAssetTypeAndAssetIdAndAssetRoleAndApplicationVersion_Application_TypeAndApplicationVersion_Status(
            FIELD,
            fieldId,
            AssetRole.PRIMARY,
            ApplicationType.PRODUCTION,
            ApplicationVersionStatus.CONSENTED
        );
  }

  @Transactional
  public void createAssetForApplicationVersion(
      ApplicationVersion applicationVersion,
      AssetKey assetKey,
      AssetRole assetRole
  ) {
    var purpose = "Adding ApplicationAsset to ApplicationVersion [%s]".formatted(applicationVersion.getId());
    var assetType = assetKey.assetType();
    var assetId = assetKey.assetId();

    switch (assetType) {
      case FIELD -> {
        var field = fieldService.getFieldWithOperator(assetId, purpose);
        var applicationAsset = createAsset(applicationVersion, field, assetRole);
        applicationAssetRepository.save(applicationAsset);
      }
      case TERMINAL -> {
        var terminal = terminalService.getTerminalWithOperator(assetId, purpose);
        var applicationAsset = createAsset(applicationVersion, terminal, assetRole);
        applicationAssetRepository.save(applicationAsset);
      }
      case FACILITY -> {
        var facility = facilityService.getFacilityWithOperator(assetId, purpose);
        var applicationAsset = createAsset(applicationVersion, facility, assetRole);
        applicationAssetRepository.save(applicationAsset);
      }
      case HUB -> {
        var hub = hubService.getHubWithOperator(assetId, purpose);
        var applicationAsset = createAsset(applicationVersion, hub, assetRole);
        applicationAssetRepository.save(applicationAsset);
      }
      default -> throw new UnsupportedOperationException("Cannot create ApplicationAsset of type [%s]".formatted(assetType));
    }
  }

  public Shore getShore(ApplicationAsset applicationAsset) {
    if (!applicationAsset.isField()) {
      throw new UnsupportedOperationException("Cannot get shore for non-field asset");
    }

    var fieldId = applicationAsset.getAssetId();
    var query = new FieldProjectionRoot().shore().root();
    var requestPurpose = new RequestPurpose("Looking up shore type for field");

    return fieldApi.findFieldById(fieldId, query, requestPurpose)
        .map(Field::getShore)
        .map(epaFieldShore -> switch (epaFieldShore) {
          case OFFSHORE -> Shore.OFFSHORE;
          case ONSHORE -> Shore.ONSHORE;
          case UNKNOWN -> Shore.UNKNOWN;
        })
        .orElseThrow(() -> new IllegalStateException("Field [%d] not found".formatted(fieldId)));
  }

  @Transactional
  public void deleteAssetsByApplicationVersionAndAssetRoles(ApplicationVersion applicationVersion, Set<AssetRole> assetRoles) {
    applicationAssetRepository.deleteAllByApplicationVersionAndAssetRoleIn(applicationVersion, assetRoles);
  }

  public Set<Integer> getAllUniqueAssetIdsForAssetType(AssetType assetType) {
    var applicableAssetRoles = Set.of(AssetRole.PRIMARY, AssetRole.SECONDARY);
    return applicationAssetRepository
        .findAllByAssetIdIsNotNullAndAssetRoleInAndAssetTypeIn(applicableAssetRoles, Set.of(assetType))
        .stream()
        .map(ApplicationAsset::getAssetId)
        .collect(Collectors.toSet());
  }

}
