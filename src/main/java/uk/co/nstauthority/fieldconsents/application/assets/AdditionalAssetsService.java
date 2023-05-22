package uk.co.nstauthority.fieldconsents.application.assets;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;

@Service
class AdditionalAssetsService {

  private final ApplicationAssetService applicationAssetService;

  private final ApplicationAssetLicenceService applicationAssetLicenceService;

  @Autowired
  AdditionalAssetsService(ApplicationAssetService applicationAssetService,
                          ApplicationAssetLicenceService applicationAssetLicenceService) {
    this.applicationAssetService = applicationAssetService;
    this.applicationAssetLicenceService = applicationAssetLicenceService;
  }

  @Transactional
  void saveAdditionalAsset(ApplicationVersion applicationVersion,
                           FieldWithOperatorAndLicencesJson field) {
    var applicationAsset = applicationAssetService.createSecondaryAsset(applicationVersion, field);
    applicationAssetLicenceService.createAssetLicences(applicationAsset, field);
  }

  @Transactional
  void deleteAdditionalAsset(ApplicationAsset asset) {
    applicationAssetLicenceService.deleteAssetLicences(asset);
    applicationAssetService.deleteSecondaryAsset(asset);
  }
}
