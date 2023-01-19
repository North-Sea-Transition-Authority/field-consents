package uk.co.nstauthority.fieldconsents.assets;

import java.util.Comparator;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Service
public class ApplicationAssetService {

  private final FieldService fieldService;

  private final TerminalService terminalService;

  private final ApplicationAssetRepository applicationAssetRepository;

  @Autowired
  public ApplicationAssetService(FieldService fieldService,
                                 TerminalService terminalService,
                                 ApplicationAssetRepository applicationAssetRepository) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.applicationAssetRepository = applicationAssetRepository;
  }

  public void createAssetRecordForPrimaryField(ApplicationVersion applicationVersion,
                                               FieldJson fieldJson) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setFieldId(fieldJson.fieldId());
    applicationAsset.setCachedFieldName(fieldJson.fieldName());
    applicationAsset.setAssetRole(AssetRole.PRIMARY);

    // TODO We should cater for this exception earlier on when creating an application - FCS-274
    if (fieldJson.operatorOuId() != null) {
      applicationAsset.setAssetOperatorOuId(fieldJson.operatorOuId());
      applicationAsset.setCachedAssetOperatorName(fieldJson.operatorName());
    } else {
      throw new RuntimeException("No operator was found for field %s with id %s."
          .formatted(fieldJson.fieldName(), fieldJson.fieldId())
      );
    }

    applicationAssetRepository.save(applicationAsset);
  }

  public void createAssetRecordForTerminal(ApplicationVersion applicationVersion,
                                           TerminalJson terminalJson) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setTerminalId(terminalJson.terminalId());
    applicationAsset.setCachedTerminalName(terminalJson.terminalName());
    applicationAsset.setAssetRole(AssetRole.PRIMARY);

    // TODO We should cater for this exception earlier on when creating an application - FCS-274
    if (terminalJson.operatorOuId() != null) {
      applicationAsset.setAssetOperatorOuId(terminalJson.operatorOuId());
      applicationAsset.setCachedAssetOperatorName(terminalJson.operatorName());
    } else {
      throw new RuntimeException("No operator was found for terminal %s with id %s."
          .formatted(terminalJson.terminalName(), terminalJson.terminalId())
      );
    }

    applicationAssetRepository.save(applicationAsset);
  }

  public ApplicationAsset getPrimaryApplicationAsset(ApplicationVersion applicationVersion) {
    return applicationAssetRepository.findByApplicationVersionAndAssetRole(applicationVersion, AssetRole.PRIMARY)
        .orElseThrow(() -> new EntityNotFoundException("Primary application asset not found for application version id %s"
            .formatted(applicationVersion.getId())));
  }

  public AssetJson getAssetJsonForApplicationAsset(ApplicationAsset applicationAsset) {
    if (applicationAsset.getFieldId() != null) {
      return fieldService.findField(applicationAsset.getFieldId(),
              "Field lookup for application asset")
          .map(AssetJson::from)
          .orElseGet(() -> AssetJson.fromCachedInformation(applicationAsset.getFieldId(),
              applicationAsset.getCachedFieldName(),
              AssetType.FIELD));
    } else if (applicationAsset.getTerminalId() != null) {
      return terminalService.findTerminal(applicationAsset.getTerminalId(),
              "Terminal lookup for application asset")
          .map(AssetJson::from)
          .orElseGet(() -> AssetJson.fromCachedInformation(applicationAsset.getTerminalId(),
              applicationAsset.getCachedTerminalName(),
              AssetType.TERMINAL));
    } else {
      throw new RuntimeException("Field and terminal ids not found for application asset id %s"
          .formatted(applicationAsset.getId()));
    }
  }

  public List<ApplicationAsset> getAdditionalAssetsForApplicationVersion(ApplicationVersion applicationVersion) {
    return applicationAssetRepository
        .findAllByApplicationVersionAndAssetRoleOrderByIdAsc(applicationVersion, AssetRole.SECONDARY);
  }

  public boolean additionalAssetsExistForApplicationVersion(ApplicationVersion applicationVersion) {
    return !getAdditionalAssetsForApplicationVersion(applicationVersion).isEmpty();
  }

  public void saveAdditionalAsset(ApplicationVersion applicationVersion, AssetJson assetJson) {

    if (assetJson.assetType().equals(AssetType.TERMINAL)) {
      throw new RuntimeException("Secondary asset is not allowed on terminal asset with id %s".formatted(assetJson.assetId()));
    }

    FieldJson fieldJson = fieldService
        .getFieldWithOperator(assetJson.assetId(), "Lookup field prior to creating a field application");

    // find the next Asset number to use
    Integer nextAssetNo = getAdditionalAssetsForApplicationVersion(applicationVersion).stream()
        .max(Comparator.comparing(ApplicationAsset::getAssetNo))
        .map(asset -> asset.getAssetNo() + 1).orElse(1);

    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setFieldId(fieldJson.fieldId());
    applicationAsset.setCachedFieldName(fieldJson.fieldName());
    applicationAsset.setAssetRole(AssetRole.SECONDARY);
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setAssetNo(nextAssetNo);

    if (fieldJson.operatorOuId() != null) {
      applicationAsset.setAssetOperatorOuId(fieldJson.operatorOuId());
      applicationAsset.setCachedAssetOperatorName(fieldJson.operatorName());
    } else {
      throw new RuntimeException("No operator was found for field %s with id %s."
          .formatted(fieldJson.fieldName(), fieldJson.fieldId())
      );
    }

    applicationAssetRepository.save(applicationAsset);
  }

  public ApplicationAsset getAsset(ApplicationVersion applicationVersion, Integer assetNo) {
    return applicationAssetRepository.findByApplicationVersionAndAssetNo(applicationVersion, assetNo)
        .orElseThrow(() ->
            new EntityNotFoundException("Asset with application version id %s and asset no %s not found"
                .formatted(applicationVersion.getId(), assetNo))
        );
  }

  public void deleteAsset(ApplicationAsset asset) {
    applicationAssetRepository.delete(asset);
  }
}
