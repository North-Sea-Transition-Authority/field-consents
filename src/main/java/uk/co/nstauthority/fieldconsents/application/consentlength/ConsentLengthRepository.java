package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface ConsentLengthRepository extends CrudRepository<ConsentLengthDetails, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  Optional<ConsentLengthDetails> findByApplicationVersion(ApplicationVersion applicationVersion);
}
