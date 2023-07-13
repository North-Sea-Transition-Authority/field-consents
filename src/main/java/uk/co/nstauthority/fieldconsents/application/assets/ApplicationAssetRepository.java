package uk.co.nstauthority.fieldconsents.application.assets;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ApplicationAssetRepository extends CrudRepository<ApplicationAsset, Integer> {

  Optional<ApplicationAsset> findByApplicationVersionAndAssetRole(
      ApplicationVersion applicationVersion,
      AssetRole assetRole
  );

  List<ApplicationAsset> findAllByApplicationVersionAndAssetRoleOrderByIdAsc(
      ApplicationVersion applicationVersion,
      AssetRole assetRole
  );

  List<ApplicationAsset> findAllByAssetRoleAndFieldIdIsNotNull(AssetRole assetRole);

  List<ApplicationAsset> findAllByAssetRoleAndTerminalIdIsNotNull(AssetRole assetRole);

  Optional<ApplicationAsset> findByApplicationVersionAndAssetNo(ApplicationVersion applicationVersion, Integer assetNo);

  Optional<ApplicationAsset> findByApplicationVersionAndFieldId(ApplicationVersion applicationVersion, Integer fieldId);

  List<ApplicationAsset> findAllByApplicationVersion(ApplicationVersion applicationVersion);
}
