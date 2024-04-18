package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@NotDuplicationSource
public interface ConsentDataRepository extends ListCrudRepository<ConsentData, UUID> {

  Optional<ConsentData> findByApplication(Application application);

  void deleteByApplication(Application application);

  @Query("""
      SELECT DISTINCT cd
      FROM ConsentData cd
      JOIN ApplicationAsset aa ON aa.applicationVersion.application = cd.application
      WHERE aa.applicationVersion.status = 'COMPLETED'
        AND aa IN :applicationAssets
      """)
  List<ConsentData> getConsentDataListForCompletedApplicationsWithAssets(Collection<ApplicationAsset> applicationAssets);

}
