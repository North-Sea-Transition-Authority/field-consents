package uk.co.nstauthority.fieldconsents.application.workareapriority;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicateThisOnUpdate;
import uk.co.nstauthority.fieldconsents.application.duplication.DuplicationSource;

@Repository
public interface ApplicationWorkAreaPriorityRepository
    extends CrudRepository<ApplicationWorkAreaPriority, Integer>, DuplicationSource {

  @DuplicateThisOnUpdate
  List<ApplicationWorkAreaPriority> findAllByApplicationVersion(ApplicationVersion applicationVersion);

  Optional<ApplicationWorkAreaPriority> findByApplicationVersionAndWorkAreaPriorityGroup(
      ApplicationVersion applicationVersion,
      ApplicationWorkAreaPriorityGroup workAreaPriorityGroup);
}
