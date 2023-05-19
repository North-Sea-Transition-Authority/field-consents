package uk.co.nstauthority.fieldconsents.application.assets;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
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

  @Autowired
  public ApplicationAssetService(FieldService fieldService,
                                 TerminalService terminalService,
                                 ApplicationAssetRepository applicationAssetRepository,
                                 ApplicationFlagService applicationFlagService) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.applicationAssetRepository = applicationAssetRepository;
    this.applicationFlagService = applicationFlagService;
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

  public List<ApplicationAsset> getSecondaryAssets(ApplicationVersion applicationVersion) {
    return applicationAssetRepository
        .findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY);
  }

  public boolean secondaryAssetsExist(ApplicationVersion applicationVersion) {
    return !getSecondaryAssets(applicationVersion).isEmpty();
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
}
