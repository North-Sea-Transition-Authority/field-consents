package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@NotDuplicationSource
public interface ConsentDataRepository extends ListCrudRepository<ConsentData, UUID> {

  Optional<ConsentData> findByApplication(Application application);

  void deleteByApplication(Application application);

  @Query("""
      SELECT
        aa.assetId AS fieldId,
        cd AS consentData
      FROM Application a
      JOIN ConsentData cd ON cd.application = a AND cd.consentStartDate <= :end AND cd.consentEndDate >= :start
      JOIN Consent c ON c.application = a
      JOIN ApplicationVersion av ON av.application = a AND av.status = 'CONSENTED'
      JOIN ApplicationAsset aa ON aa.applicationVersion = av AND aa.assetType = 'FIELD' AND aa.assetId IN :fieldIds
      WHERE a.type = 'PRODUCTION'
      AND c.supersededByConsent IS NULL
      AND (aa.assetRole = 'PRIMARY' OR aa.assetRole = 'SECONDARY')
      """)
  List<ConsentDataForFieldId> getConsentDataListInRangeForConsentedProductionApplicationsForFieldIds(
      LocalDate start,
      LocalDate end,
      Collection<Integer> fieldIds
  );

  @Query("""
      SELECT cd
      FROM Application a
      JOIN ConsentData cd ON cd.application = a
      JOIN Consent c ON c.application = a
      JOIN ApplicationVersion av ON av.application = a
      JOIN ApplicationAsset aa ON aa.applicationVersion = av
      WHERE a.type = :applicationType
      AND (:consentYear BETWEEN year(cd.consentStartDate) AND year(cd.consentEndDate))
      AND c.supersededByConsent IS NULL
      AND av.status = 'CONSENTED'
      AND aa.assetRole = 'PRIMARY'
      AND aa.assetType = :assetType
      AND aa.assetId = :assetId
      """)
  List<ConsentData> getAssetConsentDataForApplicationTypeAndConsentYear(
      ApplicationType applicationType,
      AssetType assetType,
      Integer assetId,
      Integer consentYear
  );

}
