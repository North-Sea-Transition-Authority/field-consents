package uk.co.nstauthority.fieldconsents.application.assetlicences;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;

public interface ApplicationAssetLicenceRepository extends CrudRepository<ApplicationAssetLicence, Integer> {

  List<ApplicationAssetLicence> findAllByApplicationAssetOrderByCachedLicenceRefAsc(ApplicationAsset applicationAsset);

  List<ApplicationAssetLicence> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteApplicationAssetLicencesByApplicationAsset(ApplicationAsset applicationAsset);
}
