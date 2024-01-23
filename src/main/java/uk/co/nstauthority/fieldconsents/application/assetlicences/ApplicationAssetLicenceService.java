package uk.co.nstauthority.fieldconsents.application.assetlicences;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.assets.AssetWithLicencesJson;

@Service
public class ApplicationAssetLicenceService {

  private final ApplicationAssetLicenceRepository applicationAssetLicenceRepository;

  @Autowired
  ApplicationAssetLicenceService(ApplicationAssetLicenceRepository applicationAssetLicenceRepository) {
    this.applicationAssetLicenceRepository = applicationAssetLicenceRepository;
  }

  @Transactional
  public void createAssetLicences(ApplicationAsset applicationAsset,
                                  AssetWithLicencesJson asset) {

    if (asset.getLicences() == null || asset.getLicences().isEmpty()) {
      throw new RuntimeException("No licences found for asset %s with id %s."
          .formatted(asset.getName(), asset.getId()));
    }

    asset.getLicences()
        .stream()
        .map(licence -> new ApplicationAssetLicence(
            applicationAsset.getApplicationVersion(),
            applicationAsset,
            licence.licenceId(),
            licence.licenceRef()))
        .forEach(applicationAssetLicenceRepository::save);
  }

  public List<ApplicationAssetLicence> getAssetLicences(ApplicationAsset applicationAsset) {
    return getAssetLicences(Collections.singleton(applicationAsset));
  }

  public List<ApplicationAssetLicence> getAssetLicences(Collection<ApplicationAsset> applicationAssets) {
    return applicationAssetLicenceRepository.findAllByApplicationAssetInOrderByCachedLicenceRefAsc(applicationAssets);
  }

  public List<ApplicationAssetLicence> getAssetLicences(ApplicationVersion applicationVersion) {
    return applicationAssetLicenceRepository.findAllByApplicationVersion(applicationVersion);
  }

  public Map<ApplicationAsset, List<ApplicationAssetLicence>> getAssetLicencesMap(ApplicationVersion applicationVersion) {
    return applicationAssetLicenceRepository.findAllByApplicationVersion(applicationVersion)
        .stream()
        .collect(
            groupingBy(
                ApplicationAssetLicence::getApplicationAsset,
                mapping(applicationAssetLicence -> applicationAssetLicence, Collectors.toList())
            )
        );
  }

  @Transactional
  public void deleteAssetLicences(ApplicationAsset applicationAsset) {
    applicationAssetLicenceRepository.deleteApplicationAssetLicencesByApplicationAsset(applicationAsset);
  }
}
