package uk.co.nstauthority.fieldconsents.application.unit;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface ApplicationUnitRepository extends CrudRepository<ApplicationUnit, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  Optional<ApplicationUnit> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteAllByApplicationVersion(ApplicationVersion applicationVersion);
}
