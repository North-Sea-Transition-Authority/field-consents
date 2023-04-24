package uk.co.nstauthority.fieldconsents.application;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface ApplicationRepository extends CrudRepository<Application, Integer> {

  @Query(
      """
      SELECT max(app.applicationNo)
      FROM Application app
      """
  )
  Optional<Integer> findLatestApplicationNumber();
}
