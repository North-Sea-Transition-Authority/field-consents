package uk.co.nstauthority.fieldconsents.application.assets;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@Repository
@NotDuplicationSource
public interface ApplicationAssetRepository extends CrudRepository<ApplicationAsset, Integer> {

  Optional<ApplicationAsset> findByApplicationVersionAndAssetRole(
      ApplicationVersion applicationVersion,
      AssetRole assetRole
  );

  List<ApplicationAsset> findAllByApplicationVersionAndAssetRoleInOrderByIdAsc(
      ApplicationVersion applicationVersion,
      Collection<AssetRole> assetRole
  );

  List<ApplicationAsset> findAllByApplicationVersionAndAssetTypeAndAssetRoleInOrderByIdAsc(
      ApplicationVersion applicationVersion,
      AssetType assetType,
      Collection<AssetRole> assetRole
  );

  boolean existsByApplicationVersionAndAssetRole(ApplicationVersion applicationVersion, AssetRole assetRole);

  void deleteAllByApplicationVersionAndAssetRoleIn(ApplicationVersion applicationVersion, Set<AssetRole> assetRoles);

  @EntityGraph(attributePaths = {"applicationVersion", "applicationVersion.application"})
  List<ApplicationAsset> findAllByAssetIdIsNotNullAndAssetRoleInAndAssetTypeIn(
      Set<AssetRole> assetRoles,
      Set<AssetType> assetTypes
  );

  List<ApplicationAsset> findAllByAssetRoleAndAssetTypeAndAssetIdIsNotNull(AssetRole assetRole, AssetType assetType);

  Optional<ApplicationAsset> findByApplicationVersionAndAssetNo(ApplicationVersion applicationVersion, Integer assetNo);

  Optional<ApplicationAsset> findByApplicationVersionAndAssetTypeAndAssetId(
      ApplicationVersion applicationVersion,
      AssetType assetType,
      Integer assetId
  );

  List<ApplicationAsset> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  boolean existsByAssetTypeAndAssetIdAndAssetRoleAndApplicationVersion_Application_TypeAndApplicationVersion_Status(
      AssetType assetType,
      Integer assetId,
      AssetRole assetRole,
      ApplicationType applicationType,
      ApplicationVersionStatus applicationVersionStatus
  );
}
