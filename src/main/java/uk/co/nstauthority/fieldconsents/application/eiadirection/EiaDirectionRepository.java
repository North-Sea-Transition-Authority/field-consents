package uk.co.nstauthority.fieldconsents.application.eiadirection;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface EiaDirectionRepository extends CrudRepository<EiaDirection, Integer> {

  Optional<EiaDirection> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}
