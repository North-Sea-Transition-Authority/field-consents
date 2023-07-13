package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface SupportingInformationRepository extends CrudRepository<SupportingInformation, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  Optional<SupportingInformation> findByApplicationVersion(ApplicationVersion applicationVersion);

  void deleteByApplicationVersion(ApplicationVersion applicationVersion);
}
