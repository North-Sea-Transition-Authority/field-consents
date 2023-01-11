package uk.co.nstauthority.fieldconsents.assets;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public interface ApplicationAssetRepository extends CrudRepository<ApplicationAsset, Integer> {

  Optional<ApplicationAsset> findByApplicationVersionAndAssetRole(ApplicationVersion applicationVersion,
                                                                  AssetRole assetRole);

}
