package uk.co.nstauthority.fieldconsents.application.workareapriority;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Repository
public interface ApplicationWorkAreaPriorityRepository extends CrudRepository<ApplicationWorkAreaPriority, Integer> {

  Optional<ApplicationWorkAreaPriority> findByApplicationVersionAndWorkAreaPriorityGroup(
      ApplicationVersion applicationVersion,
      ApplicationWorkAreaPriorityGroup workAreaPriorityGroup);
}
