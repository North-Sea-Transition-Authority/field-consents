package uk.co.nstauthority.fieldconsents.application.caseprocessing.update;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ApplicationUpdateRepository extends CrudRepository<ApplicationUpdate, Integer> {

  boolean existsByApplicationVersion_ApplicationAndApplicationUpdateStatus(Application application,
                                                                           ApplicationUpdateStatus status);

  @EntityGraph("applicationUpdate")
  Optional<ApplicationUpdate> findByApplicationVersion_ApplicationAndApplicationUpdateStatus(Application application,
                                                                                             ApplicationUpdateStatus status);

  @EntityGraph("applicationUpdate")
  List<ApplicationUpdate> findByApplicationVersion_Application(Application application);

  @EntityGraph("applicationUpdate")
  List<ApplicationUpdate> findByApplicationUpdateStatusAndApplicationVersion_primaryOperatorOuIdIn(
      ApplicationUpdateStatus status,
      Collection<Integer> primaryOperatorIds
  );
}
