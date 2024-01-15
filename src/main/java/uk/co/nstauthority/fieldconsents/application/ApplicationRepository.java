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
}
