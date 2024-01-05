package uk.co.nstauthority.fieldconsents.application.assetlicences;

import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ApplicationAssetLicenceRepository extends CrudRepository<ApplicationAssetLicence, Integer> {

  List<ApplicationAssetLicence> findAllByApplicationAssetInOrderByCachedLicenceRefAsc(
      Collection<ApplicationAsset> applicationAssets
  );

  List<ApplicationAssetLicence> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteApplicationAssetLicencesByApplicationAsset(ApplicationAsset applicationAsset);

  List<ApplicationAssetLicence> findAllByApplicationAsset(ApplicationAsset applicationAsset);
}
