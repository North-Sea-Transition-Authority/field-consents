package uk.co.nstauthority.fieldconsents.application.assets;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.AssetTypeWithShore;
import uk.co.nstauthority.fieldconsents.assets.AssetWithOperatorJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Service
public class ApplicationAssetService {

  private final FieldService fieldService;

  private final TerminalService terminalService;

  private final ApplicationAssetRepository applicationAssetRepository;

  private final ApplicationFlagService applicationFlagService;

  private final AssetService assetService;

  @Autowired
  public ApplicationAssetService(FieldService fieldService,
                                 TerminalService terminalService,
                                 ApplicationAssetRepository applicationAssetRepository,
                                 ApplicationFlagService applicationFlagService,
                                 AssetService assetService) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.applicationAssetRepository = applicationAssetRepository;
    this.applicationFlagService = applicationFlagService;
    this.assetService = assetService;
  }

  private ApplicationAsset createAsset(ApplicationVersion applicationVersion,
                                       AssetWithOperatorJson asset,
                                       AssetRole assetRole) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    if (asset instanceof FieldJson fieldJson) {
      applicationAsset.setFieldId(fieldJson.getId());
      applicationAsset.setCachedFieldName(fieldJson.getName());
    } else if (asset instanceof TerminalJson terminalJson) {
      applicationAsset.setTerminalId(terminalJson.getId());
      applicationAsset.setCachedTerminalName(terminalJson.getName());
    }
    applicationAsset.setAssetRole(assetRole);

    // TODO We should cater for this exception earlier on when creating an application - FCS-274
    if (asset.getOperatorJson() != null) {
      applicationAsset.setAssetOperatorOuId(asset.getOperatorJson().organisationUnitId());
      applicationAsset.setCachedAssetOperatorName(asset.getOperatorJson().name());
    } else {
      throw new RuntimeException("No operator was found for asset %s with id %s."
          .formatted(asset.getName(), asset.getId())
      );
    }

    return applicationAsset;
  }

  public ApplicationAsset createPrimaryAsset(ApplicationVersion applicationVersion,
                                             AssetWithOperatorJson asset) {
    var applicationAsset = createAsset(applicationVersion, asset, AssetRole.PRIMARY);
    return applicationAssetRepository.save(applicationAsset);
  }

  public ApplicationAsset createSecondaryAsset(ApplicationVersion applicationVersion,
                                               AssetWithOperatorJson asset) {
    if (asset.getAssetType().equals(AssetType.TERMINAL)) {
      throw new RuntimeException("Secondary asset of type terminal not allowed for application version id %s asset id %s"
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

  public List<ApplicationAsset> findAssetsByApplicationVersionAndAssetRole(ApplicationVersion applicationVersion,
                                                                           AssetRole assetRole) {
    return applicationAssetRepository.findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, assetRole);
  }

  public List<ApplicationAsset> getSecondaryAssets(ApplicationVersion applicationVersion) {
    return findAssetsByApplicationVersionAndAssetRole(applicationVersion, AssetRole.SECONDARY);
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
    if (applicationAsset.getFieldId() != null) {
      return fieldService.findField(applicationAsset.getFieldId(),
              "Field lookup for application asset")
          .orElseGet(() -> FieldJson.fromCachedInformation(applicationAsset.getFieldId(),
              applicationAsset.getCachedFieldName()));
    } else if (applicationAsset.getTerminalId() != null) {
      return terminalService.findTerminal(applicationAsset.getTerminalId(),
              "Terminal lookup for application asset")
          .orElseGet(() -> TerminalJson.fromCachedInformation(applicationAsset.getTerminalId(),
              applicationAsset.getCachedTerminalName()));
    } else {
      throw new RuntimeException("Field and terminal ids not found for application asset id %s"
          .formatted(applicationAsset.getId()));
    }
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

  public Optional<ApplicationAsset> findByApplicationVersionAndFieldId(ApplicationVersion applicationVersion, Integer fieldId) {
    return applicationAssetRepository.findByApplicationVersionAndFieldId(applicationVersion, fieldId);
  }

  public List<ApplicationAsset> findAllPrimaryFieldAssets() {
    return applicationAssetRepository.findAllByAssetRoleAndFieldIdIsNotNull(AssetRole.PRIMARY);
  }

  public List<ApplicationAsset> findAllPrimaryTerminalAssets() {
    return applicationAssetRepository.findAllByAssetRoleAndTerminalIdIsNotNull(AssetRole.PRIMARY);
  }

  public List<FieldJson> getPrimaryAndSecondaryFieldJsonsOfShoreType(List<AssetTypeWithShore> assetTypeWithShores,
                                                                     String requestPurpose) {
    var shores = assetTypeWithShores.stream()
        .map(AssetTypeWithShore::getShore)
        .toList();

    var primaryAndSecondaryFieldIds = applicationAssetRepository
        .findAllByFieldIdIsNotNullAndAssetRoleIn(EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY))
        .stream()
        .map(ApplicationAsset::getFieldId)
        .distinct()
        .toList();

    return fieldService.findFieldsByIds(primaryAndSecondaryFieldIds, requestPurpose)
        .stream()
        .filter(fieldJson -> shores.contains(fieldJson.getShore()))
        .toList();
  }

  public List<AssetJson> getAssetJsonListFor(ApplicationVersion applicationVersion, AssetRole assetRole) {
    return findAssetsByApplicationVersionAndAssetRole(applicationVersion, assetRole)
        .stream()
        .map(this::getAssetJsonFromApplicationAsset)
        .toList();
  }

  private AssetJson getAssetJsonFromApplicationAsset(ApplicationAsset applicationAsset) {
    if (applicationAsset.isTerminal()) {
      return terminalService.getTerminal(applicationAsset.getTerminalId(), "Looking up terminal reference");
    }

    if (applicationAsset.isField()) {
      return fieldService.getField(applicationAsset.getFieldId(), "Looking up field reference");
    }

    throw new UnsupportedOperationException(
        "ApplicationAsset role [%s] is unsupported".formatted(applicationAsset.getAssetRole()));
  }

  @Transactional
  public void createAssetForApplicationVersion(ApplicationVersion applicationVersion, String assetKey, AssetRole assetRole) {
    var purpose = "Adding ApplicationAsset to ApplicationVersion [%s]".formatted(applicationVersion.getId());
    var asset = assetService.getAsset(assetKey);

    if (AssetType.FIELD.equals(asset.getAssetType())) {
      var field = fieldService.getFieldWithOperator(asset.getId(), purpose);
      var applicationAsset = createAsset(applicationVersion, field, assetRole);
      applicationAssetRepository.save(applicationAsset);
      return;
    }

    if (AssetType.TERMINAL.equals(asset.getAssetType())) {
      var terminal = terminalService.getTerminalWithOperator(asset.getId(), purpose);
      var applicationAsset = createAsset(applicationVersion, terminal, assetRole);
      applicationAssetRepository.save(applicationAsset);
      return;
    }

    throw new UnsupportedOperationException("Cannot create ApplicationAsset of role [%s]".formatted(assetRole));
  }

  @Transactional
  public void deleteAssetsByApplicationVersionAndAssetRoles(ApplicationVersion applicationVersion, Set<AssetRole> assetRoles) {
    applicationAssetRepository.deleteAllByApplicationVersionAndAssetRoleIn(applicationVersion, assetRoles);
  }
}
