package uk.co.nstauthority.fieldconsents.application;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ApplicationRepository extends CrudRepository<Application, Integer> {

  @Query(
      """
      SELECT max(app.applicationNo)
      FROM Application app
      WHERE NOT EXISTS (
          SELECT 1
          FROM ApplicationVersion av
          WHERE av.application.id = app.id
          AND av.migrated = true
      )
      """
  )
  Optional<Integer> findLatestNonMigratedApplicationNumber();

  // If a revised application has two versions, SUBMITTED (1) and DELETED (2), the SUBMITTED one will be considered to
  // be the latest version as DELETED is a special status that reverts the application back to the previous version.
  @Query(
      """
      SELECT COUNT(a) > 0
      FROM Application a
      JOIN Application ra ON ra.applicationNo = a.applicationNo AND ra.variationNo > a.variationNo
      JOIN ApplicationVersion trav
        ON trav.application = ra
        AND trav.version = (
          SELECT MAX(av.version)
          FROM ApplicationVersion av
          WHERE av.application = trav.application
          AND av.status != 'DELETED'
        )
      WHERE a = :application
      AND trav.status != 'WITHDRAWN'
      AND trav.status != 'DELETED'
      """
  )
  boolean nonWithdrawnOrDeletedRevisionApplicationExists(Application application);
}
