package uk.co.nstauthority.fieldconsents.application;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.nstauthority.fieldconsents.application.duplication.NotDuplicationSource;

@Repository
@NotDuplicationSource
public interface ApplicationVersionRepository extends CrudRepository<ApplicationVersion, Integer> {

  List<ApplicationVersion> findAllByApplicationIdOrderByVersion(Integer applicationId);
}
