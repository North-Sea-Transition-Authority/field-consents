package uk.co.nstauthority.fieldconsents.application.rationale;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

public interface ApplicationRationaleRepository extends CrudRepository<ApplicationRationale, Long>, DuplicationSource {

  @DuplicateThisOnUpdate
  Optional<ApplicationRationale> findByApplicationVersion(ApplicationVersion applicationVersion);

  boolean existsApplicationRationaleByApplicationVersion(ApplicationVersion applicationVersion);

}
