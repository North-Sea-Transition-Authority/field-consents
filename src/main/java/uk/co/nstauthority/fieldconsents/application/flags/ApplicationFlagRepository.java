package uk.co.nstauthority.fieldconsents.application.flags;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface ApplicationFlagRepository extends CrudRepository<ApplicationFlag, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  List<ApplicationFlag> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  Optional<ApplicationFlag> findByApplicationVersionAndFlagType(
      ApplicationVersion applicationVersion,
      ApplicationFlagType flagType
  );
}
