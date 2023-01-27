package uk.co.nstauthority.fieldconsents.application.flags;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public interface ApplicationFlagRepository extends CrudRepository<ApplicationFlag, Integer> {

  Optional<ApplicationFlag> findByApplicationVersionAndFlagType(
      ApplicationVersion applicationVersion,
      ApplicationFlagType flagType
  );

  void deleteByApplicationVersionAndFlagType(ApplicationVersion applicationVersion, ApplicationFlagType flagType);
}
