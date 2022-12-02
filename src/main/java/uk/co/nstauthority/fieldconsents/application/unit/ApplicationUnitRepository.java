package uk.co.nstauthority.fieldconsents.application.unit;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface ApplicationUnitRepository extends CrudRepository<ApplicationUnit, Integer> {
  Optional<ApplicationUnit> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
